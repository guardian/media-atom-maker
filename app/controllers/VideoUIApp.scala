package controllers


import com.gu.media.Permissions
import com.gu.pandahmac.HMACAuthActions
import model.ClientConfig
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.Controller
import play.api.libs.concurrent.Execution.Implicits._
import util.AWSConfig

class VideoUIApp(val authActions: HMACAuthActions, conf: Configuration, awsConfig: AWSConfig) extends Controller {
  import authActions.AuthAction

  def index(id: String = "") = AuthAction.async { req =>

    val jsFileName = "video-ui/build/app.js"

    val jsLocation = sys.env.get("JS_ASSET_HOST") match {
      case Some(assetHost) => assetHost + jsFileName
      case None => routes.Assets.versioned(jsFileName).toString
    }

    val composerUrl = awsConfig.composerUrl

    Permissions.get(req.user.email).map { permissions =>
      val clientConfig = ClientConfig(
        username = req.user.email,
        youtubeEmbedUrl = "https://www.youtube.com/embed/",
        youtubeThumbnailUrl = "https://img.youtube.com/vi/",
        reauthUrl = "/reauth",
        gridUrl = awsConfig.gridUrl,
        capiProxyUrl = "/support/previewCapi",
        composerUrl = composerUrl,
        ravenUrl = conf.getString("raven.url").get,
        stage = conf.getString("stage").get,
        viewerUrl = awsConfig.viewerUrl,
        permissions
      )

      Ok(views.html.VideoUIApp.app("Media Atom Maker", jsLocation, Json.toJson(clientConfig).toString()))
    }
  }

  def reauth = AuthAction {
    Ok("auth ok")
  }

}
