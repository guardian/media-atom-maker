package com.gu.media.upload

import com.gu.media.aws.DynamoAccess
import com.gu.scanamo.syntax._
import com.gu.scanamo.{Scanamo, Table}

trait UploadsTable {
  def list(atomId: String): List[UploadEntry]
  def put(upload: UploadEntry): Unit
  def get(id: String): Option[UploadEntry]
  def delete(id: String): Unit
}

class DynamoUploadsTable(aws: DynamoAccess) extends UploadsTable {
  private val table = Table[UploadEntry](aws.uploadTrackingTableName)

  override def list(atomId: String): List[UploadEntry] = {
    val operation = table.scan()
    val allResults = Scanamo.exec(aws.dynamoDB)(operation)

    val errors = allResults.collect { case Left(err) => err }
    if(errors.nonEmpty) {
      throw DynamoUploadsTableException(errors.mkString(","))
    }

    allResults.collect { case Right(upload) if upload.atomId == atomId => upload }
  }

  override def put(upload: UploadEntry): Unit = {
    val operation = table.put(upload)
    Scanamo.exec(aws.dynamoDB)(operation)
  }

  override def get(id: String): Option[UploadEntry] = {
    val operation = table.get('id -> id)
    val result = Scanamo.exec(aws.dynamoDB)(operation)

    result.map {
      case Right(upload) => upload
      case Left(err) => throw DynamoUploadsTableException(err.toString)
    }
  }

  override def delete(id: String): Unit = {
    Scanamo.exec(aws.dynamoDB)(table.delete('id -> id))
  }
}

case class DynamoUploadsTableException(err: String) extends RuntimeException(err)
