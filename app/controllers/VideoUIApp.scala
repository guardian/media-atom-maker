package controllers

import javax.inject._
import com.gu.pandahmac.HMACAuthActions
import com.gu.pandomainauth.action.AuthActions

class VideoUIApp @Inject() (val authActions: HMACAuthActions)
  extends AtomController {

  import authActions.AuthAction
  def index(id: String = "") = AuthAction { req =>

    val jsFileName = "video-ui/build/app.js"

    val jsLocation = sys.env.get("JS_ASSET_HOST") match {
      case Some(assetHost) => assetHost + jsFileName
      case None => routes.Assets.versioned(jsFileName).toString
    }

    Ok(views.html.VideoUIApp.app("Media Atom Maker", jsLocation))
  }

  def reauth = AuthAction {
    Ok("auth ok")
  }

}
