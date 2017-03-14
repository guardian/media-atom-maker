package model.commands

import java.nio.ByteBuffer

import com.amazonaws.services.kinesis.model.{PutRecordsRequestEntry, PutRecordsRequest}
import com.gu.media.logging.Logging
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.{VideoUpload, MediaAtom}
import util.AWSConfig

class AddPlutoProjectCommand(atomId: String, plutoId: String, override val stores: DataStores, user: PandaUser,
                            awsConfig: AWSConfig)

  extends Command with Logging {

    override type T = MediaAtom

    override def process(): MediaAtom = {

      val updatedAtom = new SetPlutoIdCommand(atomId, plutoId, stores, user).process()

      for {
        upload <- plutoDataStore.get(atomId)
      } plutoDataStore.put(upload.copy(plutoProjectId = Some(plutoId)))

      val request = new PutRecordsRequest().withStreamName(awsConfig.uploadsStreamName)

      val data =
        s"""
          {
            "atomId": ${atomId}
            "plutoProjectId": ${plutoId}
            "s3Key": "key

        """.stripMargin.getBytes("UTF-8");

      val record = new PutRecordsRequestEntry()
        .withPartitionKey(atomId)
        .withData(ByteBuffer.wrap(data))

      request.withRecords(record)
      awsConfig.kinesisClient.putRecords(request)

      updatedAtom
    }
  }
