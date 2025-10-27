package data

import com.gu.media.iconik.{
  IconikCommission,
  IconikInMemoryStore,
  IconikInMemoryStoreWithParentIndex,
  IconikProject,
  IconikUpsertRequest,
  IconikWorkingGroup
}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class IconikDataStoreSpec extends AnyFlatSpec with Matchers {

  behavior of "IconikStore"

  it should "make calls to each of the underlying data stores in response to an upsert request" in {
    val projectStore =
      new IconikInMemoryStoreWithParentIndex[IconikProject, Nothing]()
    val commissionStore =
      new IconikInMemoryStoreWithParentIndex[IconikCommission, Nothing]()
    val workingGroupStore =
      new IconikInMemoryStore[IconikWorkingGroup, Nothing]()

    val store = new IconikStore[Nothing](
      projectStore,
      commissionStore,
      workingGroupStore
    )
    store.upsertIconikData(
      IconikUpsertRequest(
        id = "proj1",
        title = "Project 1",
        commissionId = "comm1",
        commissionTitle = "Commission 1",
        workingGroupId = "wg1",
        workingGroupTitle = "Working Group 1",
        status = "active",
        masterPlaceholderId = Some("mp1")
      )
    )
    projectStore.store should have size 1
    projectStore.store.head shouldEqual IconikProject(
      id = "proj1",
      title = "Project 1",
      status = "active",
      commissionId = "comm1",
      workingGroupId = "wg1",
      masterPlaceholderId = Some("mp1")
    )
    commissionStore.store should have size 1
    commissionStore.store.head shouldEqual IconikCommission(
      workingGroupId = "wg1",
      id = "comm1",
      title = "Commission 1"
    )
    workingGroupStore.store should have size 1
    workingGroupStore.store.head shouldEqual IconikWorkingGroup(
      id = "wg1",
      title = "Working Group 1"
    )
  }

}
