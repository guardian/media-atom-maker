import com.amazonaws.auth._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{AWSCredentialsProvider, AWSCredentialsProviderChain}
import com.gu.atom.play.ReindexController
import com.gu.media.aws.AwsCredentials
import com.gu.media.{Capi, MediaAtomMakerPermissionsProvider, Settings}
import com.gu.pandomainauth.PanDomainAuthSettingsRefresher
import controllers._
import data._
import play.api.ApplicationLoader.Context
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext, Configuration, LoggerConfigurator, Mode}
import play.api.inject.DefaultApplicationLifecycle
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.mvc.ControllerComponents
import play.filters.HttpFiltersComponents
import router.Routes
import util._

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

  override lazy val httpFilters = super.httpFilters.filterNot(_ == allowedHostsFilter)

  private val config = configuration.underlying

  private val credentials = environment.mode match {
    case Mode.Dev => AwsCredentials.dev(Settings(config))
    case _ => AwsCredentials.app(Settings(config))
  }

  val awsCredentialsProvider: AWSCredentialsProvider =
    new AWSCredentialsProviderChain(
      new ProfileCredentialsProvider(configuration.getOptional[String]("panda.awsCredsProfile").getOrElse("panda")),
      new InstanceProfileCredentialsProvider(false)
    )

  val panDomainSettings = new PanDomainAuthSettingsRefresher(
    domain = configuration.get[String]("panda.domain"),
    system = "video",
    actorSystem,
    awsCredentialsProvider
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

  private val youTube = YouTube(config, Duration.ofDays(1))

  private val uploaderMessageConsumer = PlutoMessageConsumer(stores, aws)
  uploaderMessageConsumer.start(actorSystem.scheduler)(actorSystem.dispatcher)

  private val thumbnailGenerator = ThumbnailGenerator(environment.getFile(s"conf/logo.png"))

  private val api = new Api(stores, configuration, hmacAuthActions, youTube, aws, permissions, capi, thumbnailGenerator, controllerComponents)

  private val stepFunctions = new StepFunctions(aws)
  private val uploads = new UploadController(hmacAuthActions, aws, stepFunctions, stores, permissions, youTube, controllerComponents)

  private val support = new Support(hmacAuthActions, capi)
  private val youTubeController = new Youtube(hmacAuthActions, youTube, permissions)

  private val plutoController = new PlutoController(config, aws, hmacAuthActions, stores, controllerComponents)

  private val youtubeTags = new YoutubeTagController(hmacAuthActions)

  private val transcoder = new util.Transcoder(aws)
  private val transcoderController = new controllers.Transcoder(hmacAuthActions, transcoder)

  private val videoApp = new VideoUIApp(hmacAuthActions, configuration, aws, permissions, youTube)

  private val login = new Login(hmacAuthActions)
  private val healthcheck = new Healthcheck(aws)
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
