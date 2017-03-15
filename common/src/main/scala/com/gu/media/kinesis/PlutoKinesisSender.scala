package com.gu.media.kinesis


import com.amazonaws.services.kinesis.model.{PutRecordsRequestEntry, PutRecordsRequest}
import com.amazonaws.services.kinesis.AmazonKinesisClient
import java.nio.ByteBuffer


object PlutoKinesisSender {

  def send(plutoId: String, s3Key: String, streamName: String, client: AmazonKinesisClient) = {

    val request = new PutRecordsRequest().withStreamName(streamName)

    val data =
      s"""
        {
          "plutoProjectId": ${plutoId}
          "s3Key": ${s3Key}

        }""".stripMargin.getBytes("UTF-8");

    println(s"sending data to kinesis ${data}")

    val record = new PutRecordsRequestEntry()
      .withPartitionKey(plutoId)
      .withData(ByteBuffer.wrap(data))

    request.withRecords(record)
    client.putRecords(request)
  }
}
