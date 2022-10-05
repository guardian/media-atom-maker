import com.amazonaws.auth._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{AWSCredentialsProvider, AWSCredentialsProviderChain}
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.gu.atom.play.ReindexController
import com.gu.media.aws.{AwsCredentials, S3Access}
import com.gu.media.{Capi, MediaAtomMakerPermissionsProvider, Settings}
import com.gu.pandomainauth.PanDomainAuthSettingsRefresher
import controllers._
import data._
import play.api.ApplicationLoader.Context
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext, Configuration, LoggerConfigurator, Mode}
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.mvc.{ControllerComponents, EssentialFilter}
import play.filters.HttpFiltersComponents
import router.Routes
import util._

import java.io.FileInputStream
import java.time.Duration

class MediaAtomMakerLoader extends ApplicationLoader {
  override def load(context: Context): Application = new MediaAtomMaker(context).application
}

class MediaAtomMaker(context: Context)
  extends BuiltInComponentsFromContext(context)
    with AhcWSComponents
    with AssetsComponents
    with HttpFiltersComponents {

  // required to start logging (https://www.playframework.com/documentation/2.5.x/ScalaCompileTimeDependencyInjection)
  LoggerConfigurator(context.environment.classLoader).foreach(_.configure(context.environment))

  override lazy val httpFilters: Seq[EssentialFilter] = super.httpFilters.filterNot(_ == allowedHostsFilter)

  private val config = configuration.underlying

  private val credentials = environment.mode match {
    case Mode.Dev => AwsCredentials.dev(Settings(config))
    case _ => AwsCredentials.app(Settings(config))
  }

  val pandaAwsCredentialsProvider: AWSCredentialsProvider =
    new AWSCredentialsProviderChain(
      new ProfileCredentialsProvider(configuration.getOptional[String]("panda.awsCredsProfile").getOrElse("panda")),
      new InstanceProfileCredentialsProvider(false)
    )

  private lazy val domain = configuration.get[String]("panda.domain")

  val panDomainSettings = new PanDomainAuthSettingsRefresher(
    domain = domain,
    system = "video",
    bucketName = configuration.getOptional[String]("panda.bucketName").getOrElse("pan-domain-auth-settings"),
    settingsFileKey = configuration.getOptional[String]("panda.settingsFileKey").getOrElse(s"$domain.settings"),
    s3Client = S3Access.buildClient(pandaAwsCredentialsProvider, "eu-west-1")
  )

  private val hmacAuthActions = new PanDomainAuthActions {
    override def conf: Configuration = MediaAtomMaker.this.configuration

    override def wsClient: WSClient = MediaAtomMaker.this.wsClient

    override def controllerComponents: ControllerComponents = MediaAtomMaker.this.controllerComponents

    override def panDomainSettings: PanDomainAuthSettingsRefresher = MediaAtomMaker.this.panDomainSettings
  }

  private val aws = new AWSConfig(config, credentials)

  private val capi = new Capi(config)

  private val stores = new DataStores(aws, capi)
  private val permissions = new MediaAtomMakerPermissionsProvider(aws.stage, aws.credentials.instance)

  private val reindexer = buildReindexer()

  private def serviceAccountCertPath = aws.stage match {
    case "PROD" | "CODE" => "/etc/gu/youtube-service-account.json"
    case _ => System.getProperty("user.home") + "/.gu/youtube-service-account.json"
  }

  private val youtubeCredentials: GoogleCredential = GoogleCredential
    .fromStream(new FileInputStream(serviceAccountCertPath))

  private val youTube = YouTube(config, Duration.ofDays(1), youtubeCredentials)

  private val uploaderMessageConsumer = PlutoMessageConsumer(stores, aws)
  uploaderMessageConsumer.start(actorSystem.scheduler)(actorSystem.dispatcher)

  private val thumbnailGenerator = ThumbnailGenerator(environment.getFile(s"conf/logo.png"))

  private val api = new Api(stores, configuration, hmacAuthActions, youTube, aws, permissions, capi, thumbnailGenerator, controllerComponents)

  private val stepFunctions = new StepFunctions(aws)
  private val uploads = new UploadController(hmacAuthActions, aws, stepFunctions, stores, permissions, youTube, controllerComponents)

  private val support = new Support(hmacAuthActions, capi, controllerComponents)
  private val youTubeController = new Youtube(hmacAuthActions, youTube, permissions, controllerComponents)

  private val plutoController = new PlutoController(config, aws, hmacAuthActions, stores, controllerComponents)

  private val youtubeTags = new YoutubeTagController(hmacAuthActions, controllerComponents)

  private val transcoder = new util.Transcoder(aws)
  private val transcoderController = new controllers.Transcoder(hmacAuthActions, transcoder, controllerComponents)

  private val videoApp = new VideoUIApp(hmacAuthActions, configuration, aws, permissions, youTube, controllerComponents)

  private val login = new Login(hmacAuthActions, controllerComponents)
  private val healthcheck = new Healthcheck(aws, controllerComponents)
  override lazy val assets = new Assets(httpErrorHandler, assetsMetadata)

  override val router = new Routes(
    httpErrorHandler,
    api,
    plutoController,
    uploads,
    youTubeController,
    youtubeTags,
    transcoderController,
    reindexer,
    videoApp,
    support,
    login,
    healthcheck,
    assets
  )

  private def buildReindexer() = {
    // pass the parameters manually since the reindexer is part of the atom-maker lib
    new ReindexController(stores.preview, stores.published, stores.reindexPreview, stores.reindexPublished,
      configuration, controllerComponents, actorSystem)

  }
}
