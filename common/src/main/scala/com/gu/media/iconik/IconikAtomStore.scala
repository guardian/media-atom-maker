package com.gu.media.iconik

import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.{
  AttributeValue,
  ScanRequest,
  TransactionCanceledException,
  TransactWriteItem,
  TransactWriteItemsRequest,
  Update
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
    projectId: Option[String],
    revision: Long = 0L
)

/** Used for repairing Iconik references with a minimal set of fields. Not part
  * of the publication flow. Scanamo is not used because its scan API lacks
  * field projection and parallel segments.
  */
trait IconikAtomStore {
  def listLinkedAtoms(): Future[Either[DynamoFailure, List[AtomIconikRef]]]

  def updateProjectParents(
      refs: List[AtomIconikRef],
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
    "#iconik" -> "iconik",
    "#changes" -> "contentChangeDetails",
    "#revision" -> "revision"
  ).asJava

  override def listLinkedAtoms()
      : Future[Either[DynamoFailure, List[AtomIconikRef]]] = {
    val previewFuture = scanTable(previewTableName, isPublished = false)
    val publishedFuture = scanTable(publishedTableName, isPublished = true)

    val linkedAtoms = for {
      preview <- previewFuture
      published <- publishedFuture
    } yield Right(preview ++ published)

    linkedAtoms.recover { case NonFatal(error) =>
      Left(
        DynamoFailure(
          "Failed to scan atom tables for Iconik references",
          Some(unwrap(error))
        )
      )
    }
  }

  override def updateProjectParents(
      refs: List[AtomIconikRef],
      projectId: String,
      commissionId: String,
      workingGroupId: String
  ): Future[Either[DynamoFailure, Boolean]] = {
    if (refs.isEmpty) Future.successful(Right(true))
    else {
      val writes = refs.map { ref =>
        TransactWriteItem
          .builder()
          .update(
            updateRequest(
              ref,
              projectId,
              commissionId,
              workingGroupId
            )
          )
          .build()
      }
      val request = TransactWriteItemsRequest
        .builder()
        .transactItems(
          writes.asJava
        )
        .build()

      dynamoDbAsync
        .transactWriteItems(request)
        .asScala
        .map(_ => Right(true))
        .recover {
          case error if hasConditionalCheckFailure(unwrap(error)) =>
            // A stale snapshot must not overwrite an editor's changes.
            Right(false)
          case NonFatal(error) =>
            Left(
              DynamoFailure(
                s"Failed to update Iconik hierarchy on atom ${refs.head.atomId}",
                Some(unwrap(error))
              )
            )
        }
    }
  }

  private def updateRequest(
      ref: AtomIconikRef,
      projectId: String,
      commissionId: String,
      workingGroupId: String
  ): Update = {
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
      "#workingGroupId" -> "workingGroupId",
      "#changes" -> "contentChangeDetails",
      "#revision" -> "revision"
    )
    val names =
      if (ref.isLegacyEncoding) baseNames
      else baseNames + ("#media" -> "media")
    val values = Map.newBuilder[String, AttributeValue]
    values += ":revision" -> AttributeValue.fromN(ref.revision.toString)
    values += ":newRevision" -> AttributeValue.fromN(
      (ref.revision + 1).toString
    )

    val expectedFields = List(
      s"$iconikPath.#projectId" -> (":currentProjectId", ref.projectId),
      s"$iconikPath.#commissionId" -> (
        ":currentCommissionId",
        ref.commissionId
      ),
      s"$iconikPath.#workingGroupId" -> (
        ":currentWorkingGroupId",
        ref.workingGroupId
      )
    )
    val fieldConditions = expectedFields.map {
      case (path, (valueName, value)) =>
        value match {
          case Some(current) =>
            values += valueName -> AttributeValue.fromS(current)
            s"$path = $valueName"
          case None => s"attribute_not_exists($path)"
        }
    }
    val shouldUpdateParents = ref.projectId.contains(projectId)
    val parentUpdates =
      if (shouldUpdateParents) {
        values += ":commissionId" -> AttributeValue.fromS(commissionId)
        values += ":workingGroupId" -> AttributeValue.fromS(workingGroupId)
        List(
          s"$iconikPath.#commissionId = :commissionId",
          s"$iconikPath.#workingGroupId = :workingGroupId"
        )
      } else Nil

    Update
      .builder()
      .tableName(tableName)
      .key(Map("id" -> AttributeValue.fromS(ref.atomId)).asJava)
      .updateExpression(
        s"SET ${(parentUpdates :+ "#changes.#revision = :newRevision").mkString(", ")}"
      )
      .conditionExpression(
        (fieldConditions :+ "#changes.#revision = :revision").mkString(" AND ")
      )
      .expressionAttributeNames(names.asJava)
      .expressionAttributeValues(values.result().asJava)
      .build()
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
      .consistentRead(true)
      .segment(segment)
      .totalSegments(scanSegments)
      .projectionExpression(
        "#id, #data.#media.#metadata.#iconik, #data.#metadata.#iconik, #changes.#revision"
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
              projectId = strField(iconik, "projectId"),
              revision = mapField(item, "contentChangeDetails")
                .flatMap(changes => Option(changes.get("revision")))
                .flatMap(value => Option(value.n()))
                .map(_.toLong)
                .getOrElse(
                  throw new IllegalStateException(
                    s"Missing revision on atom $id"
                  )
                )
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

  private def hasConditionalCheckFailure(error: Throwable): Boolean =
    error match {
      case cancelled: TransactionCanceledException =>
        Option(cancelled.cancellationReasons())
          .exists(
            _.asScala.exists(_.code() == "ConditionalCheckFailed")
          )
      case _ => false
    }

  private def unwrap(error: Throwable): Throwable = error match {
    case completion: CompletionException if completion.getCause != null =>
      completion.getCause
    case other => other
  }
}
