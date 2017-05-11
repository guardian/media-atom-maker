package controllers


import com.gu.media.MediaAtomMakerPermissionsProvider
import com.gu.pandahmac.HMACAuthActions
import model.ClientConfig
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import play.api.mvc.Controller
import util.AWSConfig

class VideoUIApp(val authActions: HMACAuthActions, conf: Configuration, awsConfig: AWSConfig,
                 permissions: MediaAtomMakerPermissionsProvider) extends Controller {
  import authActions.AuthAction

  def index(id: String = "") = AuthAction.async { req =>

    val jsFileName = "video-ui/build/app.js"

    val jsLocation = sys.env.get("JS_ASSET_HOST") match {
      case Some(assetHost) => assetHost + jsFileName
      case None => routes.Assets.versioned(jsFileName).toString
    }

    val composerUrl = awsConfig.composerUrl

    permissions.getAll(req.user.email).map { permissions =>
      val clientConfig = ClientConfig(
        username = req.user.email,
        youtubeEmbedUrl = "https://www.youtube.com/embed/",
        youtubeThumbnailUrl = "https://img.youtube.com/vi/",
        reauthUrl = "/reauth",
        gridUrl = awsConfig.gridUrl,
        capiProxyUrl = "/support/previewCapi",
        liveCapiProxyUrl = "/support/liveCapi",
        composerUrl = composerUrl,
        ravenUrl = conf.getString("raven.url").get,
        stage = conf.getString("stage").get,
        viewerUrl = awsConfig.viewerUrl,
        permissions
      )

      Ok(views.html.VideoUIApp.app("Media Atom Maker", jsLocation, Json.toJson(clientConfig).toString(), awsConfig.isDev))
    }
  }

  def reauth = AuthAction {
    Ok("auth ok")
  }

}
