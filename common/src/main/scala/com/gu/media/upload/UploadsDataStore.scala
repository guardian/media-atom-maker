package com.gu.media.upload

import com.gu.media.aws.DynamoAccess
import com.gu.scanamo.syntax._
import com.gu.scanamo.{Scanamo, Table}

class UploadsDataStore(aws: DynamoAccess) {
  private val table = Table[Upload](aws.uploadTrackingTableName)

  def list(atomId: String): List[Upload] = {
    val operation = table.scan()
    val allResults = Scanamo.exec(aws.dynamoDB)(operation)

    val errors = allResults.collect { case Left(err) => err }
    if(errors.nonEmpty) {
      throw UploadsDataStoreException(errors.mkString(","))
    }

    allResults.collect { case Right(upload) if upload.metadata.atomId == atomId => upload }
  }

  def put(upload: Upload): Unit = {
    val operation = table.put(upload)
    Scanamo.exec(aws.dynamoDB)(operation)
  }

  def report(upload: Upload): Unit = {
    get(upload.id).foreach { before =>
      val after = Upload.mergeProgress(upload, before.progress)
      put(after)
    }
  }

  def get(id: String): Option[Upload] = {
    val operation = table.get('id -> id)
    val result = Scanamo.exec(aws.dynamoDB)(operation)

    result.map {
      case Right(upload) => upload
      case Left(err) => throw UploadsDataStoreException(err.toString)
    }
  }

  def delete(id: String): Unit = {
    Scanamo.exec(aws.dynamoDB)(table.delete('id -> id))
  }
}

case class UploadsDataStoreException(err: String) extends RuntimeException(err)
