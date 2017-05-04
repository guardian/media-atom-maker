import com.gu.atom.play.ReindexController
import com.gu.media.aws.AwsCredentials
import com.gu.media.ses.Mailer
import com.gu.media.upload.actions.KinesisActionSender
import com.gu.media.youtube.{YouTube, YouTubeClaims}
import com.gu.media.{CapiPreview, MediaAtomMakerPermissionsProvider, Settings}
import controllers._
import data._
import play.api.ApplicationLoader.Context
import play.api._
import play.api.cache.EhCacheComponents
import play.api.inject.DefaultApplicationLifecycle
import play.api.libs.ws.ahc.AhcWSComponents
import router.Routes
import util.{AWSConfig, DevUploadHandler, DevUploadSender, PlutoMessageConsumer}

class MediaAtomMakerLoader extends ApplicationLoader {
  override def load(context: Context): Application = new MediaAtomMaker(context).application
}

class MediaAtomMaker(context: Context)
  extends BuiltInComponentsFromContext(context)
    with AhcWSComponents
    with EhCacheComponents {

  // required to start logging (https://www.playframework.com/documentation/2.5.x/ScalaCompileTimeDependencyInjection)
  LoggerConfigurator(context.environment.classLoader).foreach(_.configure(context.environment))

  private val config = configuration.underlying

  private val credentials = environment.mode match {
    case Mode.Dev => AwsCredentials.dev(Settings(config))
    case _ => AwsCredentials.app(Settings(config))
  }

  private val hmacAuthActions = new PanDomainAuthActions(wsClient, configuration, new DefaultApplicationLifecycle)

  private val aws = new AWSConfig(config, credentials)
  aws.startKinesisLogging("media-atom-maker")

  private val capi = new CapiPreview(config)

  private val stores = new DataStores(aws, capi)
  private val permissions = new MediaAtomMakerPermissionsProvider(aws.stage, aws.credentials.instance)

  private val reindexer = buildReindexer()

  private val youTube = new YouTube(config)

  private val youTubeClaims = new YouTubeClaims(config)

  private val sesMailer = new Mailer(aws.sesClient, configuration.getString("host").get)

  private val uploaderMessageConsumer = PlutoMessageConsumer(stores, aws)
  uploaderMessageConsumer.start(actorSystem.scheduler)(actorSystem.dispatcher)

  private val api = new Api(stores, configuration, aws, hmacAuthActions, permissions)

  private val api2 = new Api2(stores, configuration, hmacAuthActions, youTube, youTubeClaims, aws, permissions)

  private val uploadSender = buildUploadSender()
  private val uploads = new UploadController(hmacAuthActions, aws, youTube, uploadSender, stores, permissions)

  private val support = new Support(hmacAuthActions, capi)
  private val youTubeController = new controllers.Youtube(hmacAuthActions, youTube, defaultCacheApi)

  private val pluto = new PlutoProjectController(hmacAuthActions, stores)

  private val transcoder = new util.Transcoder(aws, defaultCacheApi)
  private val transcoderController = new controllers.Transcoder(hmacAuthActions, transcoder)

  private val mainApp = new MainApp(stores, wsClient, configuration, hmacAuthActions, permissions, aws)
  private val videoApp = new VideoUIApp(hmacAuthActions, configuration, aws, permissions)

  private val assets = new controllers.Assets(httpErrorHandler)

  override val router = new Routes(
    httpErrorHandler,
    mainApp,
    api,
    api2,
    pluto,
    uploads,
    youTubeController,
    transcoderController,
    reindexer,
    assets,
    videoApp,
    support
  )

  private def buildReindexer() = {
    // pass the parameters manually since the reindexer is part of the atom-maker lib
    new ReindexController(stores.preview, stores.published, stores.reindexPreview, stores.reindexPublished,
      configuration, actorSystem)

  }

  private def buildUploadSender() = aws.stage match {
    case "DEV" =>
      // Disable this case to use the lambdas, even in dev
      val handler = new DevUploadHandler(stores, aws, youTube, sesMailer)
      new DevUploadSender(handler)

    case _ =>
      new KinesisActionSender(aws)
  }
}
