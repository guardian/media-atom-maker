import com.gu.atom.play.ReindexController
import com.gu.media.CapiPreview
import com.gu.media.upload.UploadsDataStore
import com.gu.media.upload.actions.KinesisActionSender
import com.gu.media.youtube.YouTube
import controllers._
import data._
import play.api.ApplicationLoader.Context
import play.api.cache.EhCacheComponents
import play.api.inject.DefaultApplicationLifecycle
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext, LoggerConfigurator}
import router.Routes
import util.{UploaderMessageConsumer, AWSConfig, DevUploadHandler, DevUploadSender}

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
  aws.startKinesisLogging("media-atom-maker")

  private val stores = new DataStores(aws)
  private val reindexer = buildReindexer()

  private val youTube = new YouTube(config)
  private val capi = new CapiPreview(config)

  private val uploaderMessageConsumer = UploaderMessageConsumer(stores, aws)
  uploaderMessageConsumer.start(actorSystem.scheduler)(actorSystem.dispatcher)

  private val api = new Api(stores, configuration, aws, hmacAuthActions)

  private val api2 = new Api2(stores, configuration, hmacAuthActions, youTube, aws)

  private val uploadSender = buildUploadSender()
  private val uploads = new UploadController(hmacAuthActions, aws, youTube, uploadSender, stores)

  private val support = new Support(hmacAuthActions, capi)
  private val youTubeController = new controllers.Youtube(hmacAuthActions, youTube, defaultCacheApi)

  private val transcoder = new util.Transcoder(aws, defaultCacheApi)
  private val transcoderController = new controllers.Transcoder(hmacAuthActions, transcoder)

  private val mainApp = new MainApp(stores, wsClient, configuration, hmacAuthActions)
  private val videoApp = new VideoUIApp(hmacAuthActions, configuration, aws)

  private val assets = new controllers.Assets(httpErrorHandler)

  override val router =
    new Routes(httpErrorHandler, mainApp, api, api2, uploads, youTubeController, transcoderController, reindexer,
      assets, videoApp, support)

  private def buildReindexer() = {
    // pass the parameters manually since the reindexer is part of the atom-maker lib
    new ReindexController(stores.preview, stores.published, stores.reindexPreview, stores.reindexPublished,
      configuration, actorSystem)

  }

  private def buildUploadSender() = aws.stage match {
    case "DEV" =>
      // Disable this case to use the lambdas, even in dev
      val handler = new DevUploadHandler(stores, aws, youTube)
      new DevUploadSender(handler)

    case _ =>
      new KinesisActionSender(aws)
  }
}
