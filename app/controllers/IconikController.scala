package controllers

import com.gu.atom.data.IDNotFound
import com.gu.media.logging.Logging
import com.gu.media.iconik.IconikUpsertRequest
import com.gu.media.model.MediaAtom
import com.gu.pandahmac.HMACAuthActions
import com.typesafe.config.Config
import data.{DataStores, UnpackedDataStores}
import play.api.libs.json.Json
import play.api.mvc.{
  Action,
  AnyContent,
  BaseController,
  ControllerComponents,
  Cookie
}
import util.AWSConfig
import views.html.helper.CSRF

class IconikController(
    val config: Config,
    val awsConfig: AWSConfig,
    val authActions: HMACAuthActions,
    override val stores: DataStores,
    val controllerComponents: ControllerComponents
) extends BaseController
    with UnpackedDataStores
    with JsonRequestParsing
    with Logging {

  import authActions.{APIAuthAction, APIHMACAuthAction, AuthAction}

  def getWorkingGroup(groupId: String): Action[AnyContent] = APIAuthAction {
    stores.iconikDataStore.getWorkingGroup(groupId) match {
      case Right(Some(group)) => Ok(Json.toJson(group))
      case Right(None) =>
        log.warn(s"no working group with ID $groupId")
        NotFound
      case Left(error) =>
        log.error(
          s"failed to get iconik working group with ID $groupId: $error"
        )
        InternalServerError
    }
  }

  def getWorkingGroups: Action[AnyContent] = APIAuthAction {
    stores.iconikDataStore.listWorkingGroups() match {
      case Left(error) =>
        log.error(s"failed to list iconik working groups: $error")
        InternalServerError
      case Right(iconikWorkingGroups) =>
        Ok(Json.toJson(iconikWorkingGroups))
    }
  }

  def getCommissionsByWorkingGroupId(groupId: String): Action[AnyContent] =
    APIAuthAction {
      stores.iconikDataStore.getCommissionsByWorkingGroupId(groupId) match {
        case Left(error) =>
          log.error(
            s"failed to get iconik commissions for working group ID $groupId: $error"
          )
          InternalServerError
        case Right(iconikCommissions) =>
          Ok(Json.toJson(iconikCommissions))
      }
    }

  def getCommissions: Action[AnyContent] = APIAuthAction {
    stores.iconikDataStore.listCommissions() match {
      case Left(error) =>
        log.error(s"failed to list iconik commissions: $error")
        InternalServerError
      case Right(iconikCommissions) =>
        Ok(Json.toJson(iconikCommissions))
    }
  }

  def getCommission(commissionId: String): Action[AnyContent] = APIAuthAction {
    stores.iconikDataStore.getCommission(commissionId) match {
      case Right(Some(commission)) => Ok(Json.toJson(commission))
      case Right(None) =>
        log.warn(s"no commission with ID $commissionId")
        NotFound
      case Left(error) =>
        log.error(
          s"failed to get iconik commission with ID $commissionId: $error"
        )
        InternalServerError
    }
  }

  def getProjectsByCommissionId(id: String): Action[AnyContent] =
    APIAuthAction {
      stores.iconikDataStore.getProjectsByCommissionId(id) match {
        case Left(error) =>
          log.error(
            s"failed to get iconik projects for commission ID $id: $error"
          )
          InternalServerError
        case Right(iconikProjects) =>
          Ok(Json.toJson(iconikProjects))
      }
    }

  def getProject(projectId: String): Action[AnyContent] = APIAuthAction {
    stores.iconikDataStore.getProject(projectId) match {
      case Right(Some(project)) => Ok(Json.toJson(project))
      case Right(None) =>
        log.warn(s"no project with ID $projectId")
        NotFound
      case Left(error) =>
        log.error(s"failed to get iconik project with ID $projectId: $error")
        InternalServerError
    }
  }

  def deleteCommission(id: String): Action[AnyContent] = APIHMACAuthAction {
    stores.iconikDataStore.deleteProjectsByCommissionId(id) match {
      case Left(error) =>
        log.error(
          s"failed to delete iconik projects for commission ID $id: $error"
        )
        InternalServerError
      case Right(_) =>
        stores.iconikDataStore.deleteCommission(id)
        Ok
    }
  }

  def upsertIconikData(): Action[AnyContent] = APIHMACAuthAction {
    implicit req =>
      parse[IconikUpsertRequest](req) { data: IconikUpsertRequest =>
        val project = stores.iconikDataStore.upsertIconikData(data)
        Ok(Json.toJson(project))
      }
  }

  def openInIconik(atomId: String): Action[AnyContent] = AuthAction {
    implicit request =>
      previewDataStore.getAtom(atomId) match {
        case Left(IDNotFound) =>
          NotFound(
            "This atom ID does not appear to exist. Have you got the ID right?"
          )
        case Left(err) =>
          log.error(
            s"Error when trying to lookup $atomId in preview data store",
            err
          )
          InternalServerError("Error when trying to lookup atom status")
        case Right(atom) =>
          val iconikProjectId =
            MediaAtom.fromThrift(atom).iconikData.flatMap(_.projectId)
          iconikProjectId match {
            case Some(projectId) =>
              SeeOther(s"https://app.iconik.io/collection/$projectId")
            case None =>
              NotFound(
                views.html.VideoUIApp.notInIconik(atomId, CSRF.getToken.value)
              )
          }
      }
  }
}
