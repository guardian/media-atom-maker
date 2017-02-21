import com.gu.atom.data.{PreviewMediaAtomDataStoreProvider, PublishedMediaAtomDataStoreProvider}
import com.gu.atom.play.ReindexController
import com.gu.atom.publish.{LiveKinesisAtomPublisher, PreviewKinesisAtomPublisher}
import controllers._
import data._
import play.api.ApplicationLoader.Context
import play.api.cache.EhCacheComponents
import play.api.inject.DefaultApplicationLifecycle
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext, LoggerConfigurator}
import router.Routes
import util.{AWSConfig, ExpiryPoller, LogShipping, YouTubeConfig}

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

  private val aws = new AWSConfig(configuration)
  private val logging = new LogShipping(aws)

  private val previewStore = new PreviewMediaAtomDataStoreProvider(aws).get
  private val publishedStore = new PublishedMediaAtomDataStoreProvider(aws).get
  private val auditStore = new AuditDataStoreProvider(aws).get
  private val reindexPreview = new PreviewAtomReindexerProvider(aws).get()
  private val reindexPublished = new PublishedAtomReindexerProvider(aws).get()

  private val livePublisher = new LiveKinesisAtomPublisher(aws.liveKinesisStreamName, aws.kinesisClient)
  private val previewPublisher = new PreviewKinesisAtomPublisher(aws.previewKinesisStreamName, aws.kinesisClient)

  private val youTube = new YouTubeConfig(configuration)

  private val reindexer =
    new ReindexController(previewStore, publishedStore, reindexPreview, reindexPublished, configuration, actorSystem)

  private val expiryPoller =
    ExpiryPoller(previewStore, publishedStore, previewPublisher, livePublisher, youTube, auditStore, aws)

  expiryPoller.start(actorSystem.scheduler)

  private val api =
    new Api(previewStore, publishedStore, livePublisher, previewPublisher, configuration, aws, hmacAuthActions)

  private val api2 =
    new Api2(previewStore, publishedStore, livePublisher, previewPublisher, configuration, aws, hmacAuthActions, youTube, auditStore)

  private val uploads = new UploadController(hmacAuthActions, aws)

  private val support = new Support(hmacAuthActions, configuration)
  private val youTubeController = new controllers.Youtube(hmacAuthActions, youTube, defaultCacheApi)

  private val mainApp = new MainApp(previewStore, publishedStore, wsClient, configuration, hmacAuthActions)
  private val videoApp = new VideoUIApp(hmacAuthActions, configuration, aws)

  private val assets = new controllers.Assets(httpErrorHandler)

  override val router =
    new Routes(httpErrorHandler, mainApp, api, api2, uploads, youTubeController, reindexer, assets, videoApp, support)
}
