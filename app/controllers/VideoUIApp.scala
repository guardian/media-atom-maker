package controllers

import javax.inject._
import com.gu.pandahmac.HMACAuthActions
import model.ClientConfig
import play.api.libs.json.Json
import util.AWSConfig

class VideoUIApp @Inject() (val authActions: HMACAuthActions, awsConfig: AWSConfig)
  extends AtomController {

  import authActions.AuthAction
  def index(id: String = "") = AuthAction { req =>

    val jsFileName = "video-ui/build/app.js"

    val jsLocation = sys.env.get("JS_ASSET_HOST") match {
      case Some(assetHost) => assetHost + jsFileName
      case None => routes.Assets.versioned(jsFileName).toString
    }

    val composerUrl = awsConfig.composerUrl

    val clientConfig = ClientConfig(
      username = req.user.email,
      youtubeEmbedUrl = "https://www.youtube.com/embed/",
      youtubeThumbnailUrl = "https://img.youtube.com/vi/",
      reauthUrl = "/reauth",
      gridUrl = awsConfig.gridUrl,
      capiProxyUrl = "/support/previewCapi",
      composerUrl = composerUrl

    )

    Ok(views.html.VideoUIApp.app("Media Atom Maker", jsLocation, Json.toJson(clientConfig).toString()))
  }

  def reauth = AuthAction {
    Ok("auth ok")
  }

}
