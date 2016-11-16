package controllers

import javax.inject._
import com.gu.pandahmac.HMACAuthActions
import model.ClientConfig
import play.api.libs.json.Json

class VideoUIApp @Inject() (val authActions: HMACAuthActions)
  extends AtomController {

  import authActions.AuthAction
  def index(id: String = "") = AuthAction { req =>

    val jsFileName = "video-ui/build/app.js"

    val jsLocation = sys.env.get("JS_ASSET_HOST") match {
      case Some(assetHost) => assetHost + jsFileName
      case None => routes.Assets.versioned(jsFileName).toString
    }

    val clientConfig = ClientConfig(
      username = req.user.email,
      youtubeEmbedUrl = "https://www.youtube.com/embed/",
      reauthUrl = "/reauth",
      gridUrl = "https://media.gutools.co.uk"
    )

    Ok(views.html.VideoUIApp.app("Media Atom Maker", jsLocation, Json.toJson(clientConfig).toString()))
  }

  def reauth = AuthAction {
    Ok("auth ok")
  }

}
