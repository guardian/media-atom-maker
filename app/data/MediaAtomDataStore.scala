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

class PublishedMediaAtomDataStoreProvider(awsConfig: AWSConfig)
    extends Provider[PublishedDataStore] {
  def get = new PublishedDynamoDataStore[MediaAtom](awsConfig.dynamoDB, awsConfig.publishedDynamoTableName) {
    def fromAtomData = { case AtomData.Media(data) => data }
    def toAtomData(data: MediaAtom) = AtomData.Media(data)
  }
}

class PreviewMediaAtomDataStoreProvider(awsConfig: AWSConfig)
  extends Provider[PreviewDataStore] {
  def get = new PreviewDynamoDataStore[MediaAtom](awsConfig.dynamoDB, awsConfig.dynamoTableName) {
    def fromAtomData = { case AtomData.Media(data) => data }
    def toAtomData(data: MediaAtom) = AtomData.Media(data)
  }
}

