package controllers

import com.gu.media.MediaAtomMakerPermissionsProvider
import com.gu.media.logging.Logging
import com.gu.media.youtube.YouTubeAccess
import com.gu.pandahmac.HMACAuthActions
import com.gu.pandomainauth.model.User
import model.{ClientConfig, Presence}
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.{
  Action,
  AnyContent,
  BaseController,
  ControllerComponents,
  Cookie
}
import util.{AWSConfig, TrainingMode}
import views.html.helper.CSRF

import scala.concurrent.ExecutionContext

class VideoUIApp(
    val authActions: HMACAuthActions,
    conf: Configuration,
    awsConfig: AWSConfig,
    permissionsProvider: MediaAtomMakerPermissionsProvider,
    youtube: YouTubeAccess,
    val controllerComponents: ControllerComponents
) extends BaseController
    with Logging
    with TrainingMode {
  import authActions.AuthAction

  implicit lazy val executionContext: ExecutionContext = defaultExecutionContext

  def index(id: String = ""): Action[AnyContent] = AuthAction { implicit req =>
    val isTrainingMode = isInTrainingMode(req)
    val shouldShowIconikUi = req.cookies.get("showIconik") match {
      case Some(cookie) if cookie.value == "true"  => true
      case Some(cookie) if cookie.value == "false" => false
      case None => conf.get[String]("stage") != "PROD"
    }

    val jsFileName = "video-ui/build/app.js"

    val isHotReloading = sys.env.get("RELOADING") match {
      case Some("HOT") if awsConfig.isDev => true
      case _                              => false
    }

    val jsLocation = routes.Assets.versioned(jsFileName).toString

    val composerUrl = awsConfig.composerUrl

    val permissions = permissionsProvider.getAll(req.user)
    val clientConfig = ClientConfig(
      presence = presenceConfig(req.user),
      youtubeEmbedUrl = "https://www.youtube.com/embed/",
      youtubeThumbnailUrl = "https://img.youtube.com/vi/",
      reauthUrl = "/reauth",
      gridUrl = awsConfig.gridUrl,
      capiProxyUrl = "/support/previewCapi",
      liveCapiProxyUrl = "/support/liveCapi",
      composerUrl = composerUrl,
      ravenUrl = conf.get[String]("raven.url"),
      stage = conf.get[String]("stage"),
      viewerUrl = awsConfig.viewerUrl,
      permissions,
      minDurationForAds = youtube.minDurationForAds,
      isTrainingMode = isTrainingMode,
      workflowUrl = awsConfig.workflowUrl,
      targetingUrl = awsConfig.targetingUrl,
      tagManagerUrl = awsConfig.tagManagerUrl,
      showIconik = shouldShowIconikUi
    )

    Ok(
      views.html.VideoUIApp.app(
        title = "Media Atom Maker",
        jsLocation,
        presenceJsLocation = clientConfig.presence.map(_.jsLocation),
        pinboardJsLocation =
          if (permissions.pinboard) awsConfig.pinboardLoaderUrl else None,
        Json.toJson(clientConfig).toString(),
        isHotReloading,
        CSRF.getToken.value
      )
    )
  }

  def training(inTraining: Boolean): Action[AnyContent] = AuthAction { req =>
    Redirect("/", FOUND).withSession(
      req.session + ("isTrainingMode" -> inTraining.toString)
    )
  }

  private def presenceConfig(user: User): Option[Presence] = {
    conf.getOptional[String]("presence.domain") match {
      case Some(origin) =>
        Some(Presence(origin, user.firstName, user.lastName, user.email))
      case None =>
        log.warn("Presence disabled. Missing presence.domain in config")
        None
    }
  }
}
