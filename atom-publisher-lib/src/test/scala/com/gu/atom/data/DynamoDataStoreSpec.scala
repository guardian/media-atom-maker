package com.gu.atom.data

import com.gu.contentatom.thrift.Atom
import com.gu.contentatom.thrift.atom.media.MediaAtom
import org.scalatest.{ fixture, Matchers, BeforeAndAfterAll, OptionValues }

import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType._
import com.amazonaws.services.dynamodbv2.model._
import com.gu.scanamo.DynamoFormat._
import com.gu.scanamo.scrooge.ScroogeDynamoFormat._
import ScanamoUtil._

import cats.data.Xor

import com.gu.atom.TestData._

class DynamoDataStoreSpec
    extends fixture.FunSpec
    with Matchers
    with OptionValues
    with BeforeAndAfterAll {
  val tableName = "atom-test-table"

  type FixtureParam = DynamoDataStore[MediaAtom]

  def withFixture(test: OneArgTest) = {
    val db = new DynamoDataStore[MediaAtom](LocalDynamoDB.client, tableName) with MediaAtomDynamoFormats
    super.withFixture(test.toNoArgTest(db))
  }

  describe("DynamoDataStore") {
    it("should create a new atom") { dataStore =>
      dataStore.createAtom(testAtom) should equal(Xor.Right())
    }

    it("should return the atom") { dataStore =>
      dataStore.getAtom(testAtom.id).value should equal(testAtom)
    }

    it("should update the atom") { dataStore =>
      val updated = testAtom.copy(defaultHtml = "<div>updated</div>")
      dataStore.updateAtom(updated) should equal(Xor.Right())
      dataStore.getAtom(testAtom.id).value should equal(updated)
    }
  }

  override def beforeAll() = {
    val client = LocalDynamoDB.client
    LocalDynamoDB.createTable(client)(tableName)('id -> S)
  }
}
