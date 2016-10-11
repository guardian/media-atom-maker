package controllers

import play.api.mvc._
import javax.inject._
import com.gu.pandomainauth.action.AuthActions

class VideoUIApp @Inject() (val authActions: AuthActions)
      extends AtomController {

  def index(id: String = "") = Action {

      val jsFileName = "video-ui/build/app.js"

      val jsLocation = sys.env.get("JS_ASSET_HOST") match {
        case Some(assetHost) => assetHost + jsFileName
        case None => routes.Assets.versioned(jsFileName).toString
      }

      Ok(views.html.VideoUIApp.app("Media Atom Maker", jsLocation))
    }

}
