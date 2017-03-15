package model.commands

import com.gu.media.logging.Logging
import com.gu.media.kinesis.PlutoKinesisSender
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.MediaAtom
import util.AWSConfig

class AddPlutoProjectCommand(atomId: String, plutoId: String, override val stores: DataStores, user: PandaUser,
                            awsConfig: AWSConfig)

  extends Command with Logging {

    override type T = MediaAtom

    override def process(): MediaAtom = {

      val updatedAtom = new SetPlutoIdCommand(atomId, plutoId, stores, user).process()

      plutoDataStore.getUploadsWithAtomId(atomId).map(upload =>
        PlutoKinesisSender.send(plutoId, upload.s3Key, awsConfig.uploadsStreamName, awsConfig.kinesisClient)
      )

      updatedAtom
    }
  }
