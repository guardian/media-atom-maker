package com.gu.atom.data

import javax.inject.{ Inject, Provider }
import util.AWSConfig

import com.gu.scanamo.DynamoFormat
import com.gu.scanamo.scrooge.ScroogeDynamoFormat._
import com.gu.contentatom.thrift._
import com.gu.contentatom.thrift.atom.media._

import scala.reflect.classTag

import DynamoFormat._

import ScanamoUtil._

class MediaAtomDataStoreProvider @Inject() (awsConfig: AWSConfig)
    extends Provider[DataStore] {
  def get = new DynamoDataStore[MediaAtom](awsConfig.dynamoDB, awsConfig.dynamoTableName,
    awsConfig.publishedDynamoTableName) {
    def fromAtomData = { case AtomData.Media(data) => data }
    def toAtomData(data: MediaAtom) = AtomData.Media(data)
  }
}
