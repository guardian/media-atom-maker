package data

import com.gu.media.iconik.{
  IconikCommission,
  IconikDataStore,
  IconikDataStoreWithParentIndex,
  IconikProject,
  IconikUpsertRequest,
  IconikWorkingGroup
}
import com.gu.media.logging.Logging

class IconikStore[E](
    projectStore: IconikDataStoreWithParentIndex[IconikProject, E],
    commissionStore: IconikDataStoreWithParentIndex[IconikCommission, E],
    workingGroupStore: IconikDataStore[IconikWorkingGroup, E]
) extends Logging {

  def getProject(projectId: String): Either[E, Option[IconikProject]] =
    projectStore.getById(projectId)
  def getProjectsByCommissionId(
      commissionId: String
  ): Either[E, List[IconikProject]] = {
    projectStore.getByParentId(
      commissionId
    )

  }
  def getCommission(commissionId: String): Either[E, Option[IconikCommission]] =
    commissionStore.getById(commissionId)
  def getCommissionsByWorkingGroupId(
      workingGroupId: String
  ): Either[E, List[IconikCommission]] = {
    commissionStore.getByParentId(workingGroupId)
  }
  def getWorkingGroup(
      workingGroupId: String
  ): Either[E, Option[IconikWorkingGroup]] =
    workingGroupStore.getById(workingGroupId)

  def listProjects(): Either[E, List[IconikProject]] = projectStore.list
  def listCommissions(): Either[E, List[IconikCommission]] =
    commissionStore.list
  def listWorkingGroups(): Either[E, List[IconikWorkingGroup]] =
    workingGroupStore.list

  def upsertIconikData(request: IconikUpsertRequest): IconikProject = {
    val project = IconikProject.fromUpsertRequest(request)
    projectStore.upsert(project)
    commissionStore.upsert(IconikCommission.fromUpsertRequest(request))
    workingGroupStore.upsert(IconikWorkingGroup.fromUpsertRequest(request))
    project
  }

  def deleteProjectsByCommissionId(commissionId: String): Either[E, Unit] = {
    // Get all projects for this commission and delete them
    projectStore.getByParentId(commissionId).map { projects =>
      projects.foreach(project => projectStore.deleteById(project.id))
    }
  }

  def deleteCommission(commissionId: String): Unit = {
    commissionStore.deleteById(commissionId)
  }

}
