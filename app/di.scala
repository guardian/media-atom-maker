import com.gu.atom.play.ReindexController
import com.gu.media.youtube.YouTubeConfig
import controllers._
import data._
import play.api.ApplicationLoader.Context
import play.api.cache.EhCacheComponents
import play.api.inject.DefaultApplicationLifecycle
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext, LoggerConfigurator}
import router.Routes
import util.{AWSConfig, ExpiryPoller}

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

  private val hmacAuthActions = new PanDomainAuthActions(wsClient, configuration, new DefaultApplicationLifecycle)

  private val aws = new AWSConfig(config)
  private val loggingCredsProvider = aws.getCrossAccountCredentials("media-atom-maker-logging")
  aws.startKinesisLogging(loggingCredsProvider)

  private val stores = new DataStores(aws)

  private val youTube = new YouTubeConfig(config)

  private val reindexer = new ReindexController(stores.preview, stores.published, stores.reindexPreview,
    stores.reindexPublished, configuration, actorSystem)

  private val expiryPoller = ExpiryPoller(stores, youTube, aws)
  expiryPoller.start(actorSystem.scheduler)

  private val api = new Api(stores, aws, hmacAuthActions)
  private val api2 = new Api2(stores, hmacAuthActions, youTube)
  private val uploads = new UploadController(hmacAuthActions, aws)

  private val support = new Support(hmacAuthActions, configuration)
  private val youTubeController = new YouTubeController(hmacAuthActions, youTube, defaultCacheApi)

  private val mainApp = new MainApp(stores, configuration, hmacAuthActions)
  private val videoApp = new VideoUIApp(hmacAuthActions, configuration, aws)

  private val assets = new controllers.Assets(httpErrorHandler)

  override val router =
    new Routes(httpErrorHandler, mainApp, api, api2, uploads, youTubeController, reindexer, assets, videoApp, support)
}
