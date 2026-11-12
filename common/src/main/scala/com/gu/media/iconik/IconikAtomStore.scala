package com.gu.media.iconik

import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.{
  AttributeValue,
  ConditionalCheckFailedException,
  ScanRequest,
  UpdateItemRequest
}

import java.util.concurrent.CompletionException
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.jdk.FutureConverters._
import scala.util.control.NonFatal

case class AtomIconikRef(
    atomId: String,
    isPublished: Boolean,
    isLegacyEncoding: Boolean,
    workingGroupId: Option[String],
    commissionId: Option[String],
    projectId: Option[String]
)

trait IconikAtomStore {
  def listLinkedAtoms(): Future[Either[DynamoFailure, List[AtomIconikRef]]]

  def updateProjectParents(
      ref: AtomIconikRef,
      projectId: String,
      commissionId: String,
      workingGroupId: String
  ): Future[Either[DynamoFailure, Boolean]]
}

class DynamoIconikAtomStore(
    dynamoDbAsync: DynamoDbAsyncClient,
    previewTableName: String,
    publishedTableName: String,
    scanSegments: Int = 4
)(implicit executionContext: ExecutionContext)
    extends IconikAtomStore {

  private val scanAttributeNames = Map(
    "#id" -> "id",
    "#data" -> "data",
    "#media" -> "media",
    "#metadata" -> "metadata",
    "#iconik" -> "iconik"
  ).asJava

  override def listLinkedAtoms()
      : Future[Either[DynamoFailure, List[AtomIconikRef]]] = {
    val previewFuture = scanTable(previewTableName, isPublished = false)
    val publishedFuture = scanTable(publishedTableName, isPublished = true)

    val linkedAtoms = for {
      preview <- previewFuture
      published <- publishedFuture
    } yield preview ++ published

    linkedAtoms.map(Right(_)).recover { case NonFatal(error) =>
      Left(
        DynamoFailure(
          "Failed to scan atom tables for Iconik references",
          Some(unwrap(error))
        )
      )
    }
  }

  override def updateProjectParents(
      ref: AtomIconikRef,
      projectId: String,
      commissionId: String,
      workingGroupId: String
  ): Future[Either[DynamoFailure, Boolean]] = {
    val tableName =
      if (ref.isPublished) publishedTableName else previewTableName
    val iconikPath =
      if (ref.isLegacyEncoding) "#data.#metadata.#iconik"
      else "#data.#media.#metadata.#iconik"

    val baseNames = Map(
      "#data" -> "data",
      "#metadata" -> "metadata",
      "#iconik" -> "iconik",
      "#projectId" -> "projectId",
      "#commissionId" -> "commissionId",
      "#workingGroupId" -> "workingGroupId"
    )
    val names =
      if (ref.isLegacyEncoding) baseNames
      else baseNames + ("#media" -> "media")

    val request = UpdateItemRequest
      .builder()
      .tableName(tableName)
      .key(Map("id" -> str(ref.atomId)).asJava)
      .updateExpression(
        s"SET $iconikPath.#commissionId = :commissionId, $iconikPath.#workingGroupId = :workingGroupId"
      )
      .conditionExpression(s"$iconikPath.#projectId = :projectId")
      .expressionAttributeNames(names.asJava)
      .expressionAttributeValues(
        Map(
          ":projectId" -> str(projectId),
          ":commissionId" -> str(commissionId),
          ":workingGroupId" -> str(workingGroupId)
        ).asJava
      )
      .build()

    dynamoDbAsync
      .updateItem(request)
      .asScala
      .map(_ => Right(true))
      .recover {
        case error
            if unwrap(error).isInstanceOf[ConditionalCheckFailedException] =>
          // Skip if an editor concurrently changed or cleared the atom's project assignment
          Right(false)
        case NonFatal(error) =>
          Left(
            DynamoFailure(
              s"Failed to update Iconik hierarchy on atom ${ref.atomId}",
              Some(unwrap(error))
            )
          )
      }
  }

  private def scanTable(
      tableName: String,
      isPublished: Boolean
  ): Future[List[AtomIconikRef]] =
    Future
      .traverse((0 until scanSegments).toList) { segment =>
        scanSegment(tableName, isPublished, segment, None, Nil)
      }
      .map(_.flatten)

  private def scanSegment(
      tableName: String,
      isPublished: Boolean,
      segment: Int,
      startKey: Option[java.util.Map[String, AttributeValue]],
      accumulated: List[AtomIconikRef]
  ): Future[List[AtomIconikRef]] = {
    val builder = ScanRequest
      .builder()
      .tableName(tableName)
      .segment(segment)
      .totalSegments(scanSegments)
      .projectionExpression(
        "#id, #data.#media.#metadata.#iconik, #data.#metadata.#iconik"
      )
      .filterExpression(
        "attribute_exists(#data.#media.#metadata.#iconik) OR attribute_exists(#data.#metadata.#iconik)"
      )
      .expressionAttributeNames(scanAttributeNames)

    startKey.foreach(builder.exclusiveStartKey)

    dynamoDbAsync.scan(builder.build()).asScala.flatMap { response =>
      val pageRefs = response
        .items()
        .asScala
        .iterator
        .flatMap(extractRef(_, isPublished))
        .toList
      val nextKey =
        Option(response.lastEvaluatedKey()).filterNot(_.isEmpty)
      nextKey match {
        case Some(key) =>
          scanSegment(
            tableName,
            isPublished,
            segment,
            Some(key),
            accumulated ++ pageRefs
          )
        case None =>
          Future.successful(accumulated ++ pageRefs)
      }
    }
  }

  private def extractRef(
      item: java.util.Map[String, AttributeValue],
      isPublished: Boolean
  ): Option[AtomIconikRef] = {
    val atomId = Option(item.get("id")).flatMap(v => Option(v.s()))
    val dataMap = Option(item.get("data"))
      .filter(_.hasM)
      .map(_.m())

    atomId.flatMap { id =>
      dataMap.flatMap { data =>
        val unionIconik = mapField(data, "media")
          .flatMap(mapField(_, "metadata"))
          .flatMap(mapField(_, "iconik"))
        val legacyIconik =
          if (data.containsKey("media")) None
          else mapField(data, "metadata").flatMap(mapField(_, "iconik"))

        unionIconik
          .map(_ -> false)
          .orElse(legacyIconik.map(_ -> true))
          .map { case (iconik, isLegacy) =>
            AtomIconikRef(
              atomId = id,
              isPublished = isPublished,
              isLegacyEncoding = isLegacy,
              workingGroupId = strField(iconik, "workingGroupId"),
              commissionId = strField(iconik, "commissionId"),
              projectId = strField(iconik, "projectId")
            )
          }
      }
    }
  }

  private def mapField(
      map: java.util.Map[String, AttributeValue],
      key: String
  ): Option[java.util.Map[String, AttributeValue]] =
    Option(map.get(key)).filter(_.hasM).map(_.m())

  private def strField(
      map: java.util.Map[String, AttributeValue],
      key: String
  ): Option[String] =
    Option(map.get(key)).flatMap(v => Option(v.s())).filter(_.nonEmpty)

  private def str(value: String): AttributeValue =
    AttributeValue.builder().s(value).build()

  private def unwrap(error: Throwable): Throwable = error match {
    case completion: CompletionException if completion.getCause != null =>
      completion.getCause
    case other => other
  }
}
