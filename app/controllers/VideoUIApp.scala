package controllers


import com.gu.media.MediaAtomMakerPermissionsProvider
import com.gu.media.logging.Logging
import com.gu.media.youtube.YouTubeAccess
import com.gu.pandahmac.HMACAuthActions
import com.gu.pandomainauth.model.User
import model.{ClientConfig, Presence}
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import play.api.mvc.Controller
import util.{AWSConfig, TrainingMode}

class VideoUIApp(val authActions: HMACAuthActions, conf: Configuration, awsConfig: AWSConfig,
                 permissions: MediaAtomMakerPermissionsProvider, youtube: YouTubeAccess) extends Controller with Logging with TrainingMode {
  import authActions.AuthAction

  def index(id: String = "") = AuthAction.async { req =>
    val isTrainingMode = isInTrainingMode(req)

    val jsFileName = "video-ui/build/app.js"

    val jsAssetHost = sys.env.get("JS_ASSET_HOST")

    val isHotReloading = jsAssetHost match {
      case Some(_) if awsConfig.isDev => true
      case _ => false
    }

    val jsLocation = if (isHotReloading) {
      jsAssetHost.get + jsFileName
    } else {
      routes.Assets.versioned(jsFileName).toString
    }

    val composerUrl = awsConfig.composerUrl

    permissions.getAll(req.user.email).map { permissions =>
      val clientConfig = ClientConfig(
        presence = presenceConfig(req.user),
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
        permissions,
        minDurationForAds = youtube.minDurationForAds,
        isTrainingMode = isTrainingMode,
        workflowUrl = awsConfig.workflowUrl
      )

      Ok(views.html.VideoUIApp.app(
        title = "Media Atom Maker",
        jsLocation,
        presenceJsLocation = clientConfig.presence.map(_.jsLocation),
        Json.toJson(clientConfig).toString(),
        isHotReloading)
      )
    }
  }

  def training(inTraining: Boolean) = AuthAction { req =>
    Redirect("/", FOUND).withSession(
      req.session + ("isTrainingMode" -> inTraining.toString)
    )
  }

  private def presenceConfig(user: User): Option[Presence] = {
    conf.getString("presence.domain") match {
      case Some(origin) =>
        Some(Presence(origin, user.firstName, user.lastName, user.email))
      case None =>
        log.warn("Presence disabled. Missing presence.domain in config")
        None
    }
  }
}
