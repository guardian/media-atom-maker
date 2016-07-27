package com.gu.atom.data

import javax.inject.{ Inject, Provider }
import util.AWSConfig

import com.gu.scanamo.DynamoFormat
import com.gu.scanamo.scrooge.ScroogeDynamoFormat._
import com.gu.contentatom.thrift._
import com.gu.contentatom.thrift.atom.media._

import scala.reflect.classTag

import DynamoFormat._

class MediaAtomDataStoreProvider @Inject() (awsConfig: AWSConfig)
    extends Provider[DataStore] {
  def get = new DynamoDataStore[MediaAtom](awsConfig.dynamoDB, awsConfig.dynamoTableName) {
    def fromAtomData = { case AtomData.Media(data) => data }
    def toAtomData(data: MediaAtom) = AtomData.Media(data)
  }
}

//object MediaAtomDataStoreHolder {

  //implicit val fmt = DynamoFormat[Seq[com.gu.contentatom.thrift.atom.media.Asset]]

  // class MediaAtomDataStore @Inject() (awsConfig: AWSConfig)
  //     extends DynamoDataStore[MediaAtom](awsConfig.dynamoDB, awsConfig.dynamoTableName)
  // with MediaAtomDynamoFormats

//}
