package model.commands

import com.amazonaws.services.sns.model.PublishRequest
import com.gu.media.logging.Logging
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.MediaAtom
import util.AWSConfig

class AddPlutoProjectCommand(atomId: String, plutoId: String, override val stores: DataStores, user: PandaUser,
                            awsConfig: AWSConfig)

  extends Command with Logging {

    override type T = MediaAtom

    override def process(): MediaAtom = {
      val snsReq = new PublishRequest(awsConfig.plutoTopicArn, atomId)
      awsConfig.snsClient.publish(snsReq)

      val updatedAtom = new SetPlutoIdCommand(atomId, plutoId, stores, user).process()
      plutoDataStore.deleteAtomFromPlutoDynamo(atomId)
      updatedAtom
    }
  }
