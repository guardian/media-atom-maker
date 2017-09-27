import com.gu.atom.play.ReindexController
import com.gu.media.aws.AwsCredentials
import com.gu.media.youtube.YouTubeClaims
import com.gu.media.{Capi, MediaAtomMakerPermissionsProvider, Settings}
import controllers._
import data._
import play.api.ApplicationLoader.Context
import play.api._
import play.api.cache.EhCacheComponents
import play.api.inject.DefaultApplicationLifecycle
import play.api.libs.ws.ahc.AhcWSComponents
import router.Routes
import util._
import scala.concurrent.duration._

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

  private val capi = new Capi(config)

  private val stores = new DataStores(aws, capi)
  private val permissions = new MediaAtomMakerPermissionsProvider(aws.stage, aws.credentials.instance)

  private val reindexer = buildReindexer()

  private val youTube = YouTube(config, defaultCacheApi, 1.hour)

  private val uploaderMessageConsumer = PlutoMessageConsumer(stores, aws)
  uploaderMessageConsumer.start(actorSystem.scheduler)(actorSystem.dispatcher)

  private val api = new Api(stores, configuration, aws, hmacAuthActions, permissions)

  private val api2 = new Api2(stores, configuration, hmacAuthActions, youTube, aws, permissions, capi)

  private val stepFunctions = new StepFunctions(aws)
  private val uploads = new UploadController(hmacAuthActions, aws, stepFunctions, stores, permissions, youTube)

  private val support = new Support(hmacAuthActions, capi)
  private val youTubeController = new controllers.Youtube(hmacAuthActions, youTube)

  private val pluto = new PlutoProjectController(hmacAuthActions, stores)

  private val youtubeTags = new YoutubeTagController(hmacAuthActions)

  private val transcoder = new util.Transcoder(aws, defaultCacheApi)
  private val transcoderController = new controllers.Transcoder(hmacAuthActions, transcoder)

  private val mainApp = new MainApp(stores, wsClient, configuration, hmacAuthActions, permissions, aws)
  private val videoApp = new VideoUIApp(hmacAuthActions, configuration, aws, permissions, youTube)

  private val assets = new controllers.Assets(httpErrorHandler)

  override val router = new Routes(
    httpErrorHandler,
    mainApp,
    api,
    api2,
    pluto,
    youtubeTags,
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
}
