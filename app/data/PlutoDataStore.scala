package data

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.gu.scanamo.Scanamo
import model.VideoUpload


class PlutoDataStore(client: AmazonDynamoDBClient, plutoDynamoTableName: String) {

  def getAtomsWithoutPlutoId(): List[VideoUpload] = {
    Scanamo.scan[VideoUpload](client)(plutoDynamoTableName)
      .foldRight(List[VideoUpload]())((result, acc) => {
        result match {
          case Left(error) => {
            acc
          }
          case Right(result: VideoUpload) => result :: acc
        }
      })
  }

  def deleteAtomFromPlutoDynamo(id: String): Unit = {
    //Scanamo.delete(client)(plutoDynamoTableName)('id -> id)
    //  Ok()
    println("delete")
  }

}
