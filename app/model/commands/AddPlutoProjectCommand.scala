package model.commands

import java.nio.ByteBuffer

import com.amazonaws.services.kinesis.model.{PutRecordsRequestEntry, PutRecordsRequest}
import com.amazonaws.services.sns.model.PublishRequest
import com.gu.media.logging.Logging
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.MediaAtom
import util.AWSConfig
import com.amazonaws.services.kinesis.AmazonKinesisClient

class AddPlutoProjectCommand(atomId: String, plutoId: String, override val stores: DataStores, user: PandaUser,
                            awsConfig: AWSConfig)

  extends Command with Logging {

    override type T = MediaAtom

    override def process(): MediaAtom = {

      val updatedAtom = new SetPlutoIdCommand(atomId, plutoId, stores, user).process()
      plutoDataStore.delete(atomId)

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
