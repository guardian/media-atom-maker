package com.gu.atom.data

import com.gu.contentatom.thrift.Atom
import com.gu.contentatom.thrift.atom.media.MediaAtom
import org.scalatest.{ fixture, Matchers, BeforeAndAfterAll, OptionValues }

import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType._
import com.amazonaws.services.dynamodbv2.model._
import com.gu.scanamo.DynamoFormat._
import com.gu.scanamo.scrooge.ScroogeDynamoFormat._
import ScanamoUtil._

import com.gu.atom.util.AtomImplicitsGeneral

import cats.data.Xor

import com.gu.atom.TestData._

class DynamoDataStoreSpec
    extends fixture.FunSpec
    with Matchers
    with OptionValues
    with BeforeAndAfterAll
    with AtomImplicitsGeneral {
  val tableName = "atom-test-table"
  val publishedTableName = "published-atom-test-table"

  case class DataStores(preview: PreviewDynamoDataStore[MediaAtom],
                        published: PublishedDynamoDataStore[MediaAtom])

  type FixtureParam = DataStores

  def withFixture(test: OneArgTest) = {
    val previewDb = new PreviewDynamoDataStore[MediaAtom](LocalDynamoDB.client, tableName) with MediaAtomDynamoFormats
    val publishedDb = new PublishedDynamoDataStore[MediaAtom](LocalDynamoDB.client, tableName) with MediaAtomDynamoFormats
    super.withFixture(test.toNoArgTest(DataStores(previewDb, publishedDb)))
  }

  describe("DynamoDataStore") {
    it("should create a new atom") { dataStores =>
      dataStores.preview.createAtom(testAtom) should equal(Xor.Right())
    }

    it("should return the atom") { dataStores =>
      dataStores.preview.getAtom(testAtom.id).value should equal(testAtom)
    }

    it("should update the atom") { dataStores =>
      val updated = testAtom
        .copy(defaultHtml = "<div>updated</div>")
        .bumpRevision

      dataStores.preview.updateAtom(updated) should equal(Xor.Right())
      dataStores.preview.getAtom(testAtom.id).value should equal(updated)
    }

    it("should update a published atom") { dataStores =>
      val updated = testAtom
        .copy()
        .withRevision(1)

      dataStores.published.updateAtom(updated) should equal(Xor.Right())
      dataStores.published.getAtom(testAtom.id).value should equal(updated)
    }
  }

  override def beforeAll() = {
    val client = LocalDynamoDB.client
    LocalDynamoDB.createTable(client)(tableName)('id -> S)
    LocalDynamoDB.createTable(client)(publishedTableName)('id -> S)
  }
}
