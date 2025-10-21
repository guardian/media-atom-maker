package controllers

import com.gu.media.logging.Logging
import com.gu.media.iconik.IconikUpsertRequest
import com.gu.pandahmac.HMACAuthActions
import com.typesafe.config.Config
import data.{DataStores, UnpackedDataStores}
import play.api.libs.json.Json
import play.api.mvc.{BaseController, ControllerComponents}
import util.AWSConfig

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

  import authActions.{APIAuthAction, APIHMACAuthAction}

  def getWorkingGroup(groupId: String) = APIAuthAction {
    stores.iconikWorkingGroupStore.getById(groupId) match {
      case Some(Right(group)) => Ok(Json.toJson(group))
      case Some(Left(error)) =>
        log.error(s"failed to get iconik working group with ID $groupId", error)
        ServiceUnavailable
      case None =>
        log.warn(s"no working group with ID $groupId")
        NotFound
    }
  }

  def getWorkingGroups() = APIAuthAction {
    val iconikWorkingGroups = stores.iconikWorkingGroupStore.list()
    Ok(Json.toJson(iconikWorkingGroups))
  }

  def getCommissionsByWorkingGroupId(groupId: String) = APIAuthAction {
    val iconikCommissions =
      stores.iconikCommissionStore.getByWorkingGroupId(groupId)
    Ok(Json.toJson(iconikCommissions))
  }

  def getCommissions() = APIAuthAction {
    val iconikCommissions = stores.iconikCommissionStore.list()
    Ok(Json.toJson(iconikCommissions))
  }

  def getCommission(commissionId: String) = APIAuthAction {
    stores.iconikCommissionStore.getById(commissionId) match {
      case Some(Right(commission)) => Ok(Json.toJson(commission))
      case Some(Left(error)) =>
        log.error(
          s"failed to get iconik commission with ID $commissionId",
          error
        )
        ServiceUnavailable
      case None =>
        log.warn(s"no commission with ID $commissionId")
        NotFound
    }
  }

  def getProjectsByCommissionId(id: String) = APIAuthAction {
    val iconikProjects = stores.iconikProjectStore.getByCommissionId(id)
    Ok(Json.toJson(iconikProjects))
  }

  def getProject(projectId: String) = APIAuthAction {
    stores.iconikProjectStore.getById(projectId) match {
      case Some(Right(project)) => Ok(Json.toJson(project))
      case Some(Left(error)) =>
        log.error(s"failed to get iconik project with ID $projectId", error)
        ServiceUnavailable
      case None =>
        log.warn(s"no project with ID $projectId")
        NotFound
    }
  }

  def deleteCommission(id: String) = APIHMACAuthAction {
    // todo -- check what the deletions story is for iconik
    stores.iconikProjectStore.deleteByCommissionId(id)
    stores.iconikCommissionStore.delete(id)
    Accepted
  }

  def upsertProject() = APIHMACAuthAction { implicit req =>
    parse[IconikUpsertRequest](req) { data: IconikUpsertRequest =>
      {
        val project = stores.iconikProjectStore.upsert(data)
        Ok(Json.toJson(project))
      }
    }
  }

}
