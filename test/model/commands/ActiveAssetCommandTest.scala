package model.commands

import com.gu.pandomainauth.model.User
import data.DataStores
import org.mockito.MockitoSugar.mock
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import util.{AWSConfig, ActivateAssetRequest, YouTube}

class ActiveAssetCommandTest extends AnyFlatSpec with Matchers {

  val atomId = "ace3fcf6-1378-41db-9d21-f3fc07072ab2"
  val stores = mock[DataStores]
  val youtube = mock[YouTube]
  val user = mock[User]
  val awsConfig = mock[AWSConfig]
  val command = ActiveAssetCommand(atomId, ActivateAssetRequest(atomId, 3L), stores, youtube, user, awsConfig)

  "firstFrameImageName" should "return the name of the first frame image associated with an mp4 video" in {
    val mp4Name = "Loop__Japan_fireball--ace3fcf6-1378-41db-9d21-f3fc07072ab2-3.0.mp4"
    val expected = "Loop__Japan_fireball--ace3fcf6-1378-41db-9d21-f3fc07072ab2-3.0.0000000.jpg"
    command.firstFrameImageName(mp4Name) shouldBe expected
  }
}
