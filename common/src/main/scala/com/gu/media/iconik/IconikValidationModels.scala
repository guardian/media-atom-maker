package com.gu.media.iconik

import play.api.libs.json._

import scala.util.{Failure, Success, Try}

case class IconikCredentials(appId: String, token: String)

object IconikCredentials {
  def fromSecret(secret: String): IconikCredentials = {
    if (secret == null || secret.isEmpty)
      throw new IllegalStateException("Iconik credentials secret is empty")

    val credentials = Try(Json.parse(secret)) match {
      case Success(value) => value
      case Failure(error) =>
        throw new IllegalStateException(
          "Iconik credentials secret is not valid JSON",
          error
        )
    }
    credentials match {
      case json: JsObject =>
        IconikCredentials(
          requiredCredential(json, "app-id"),
          requiredCredential(json, "token")
        )
      case _ =>
        throw new IllegalStateException(
          "Iconik credentials secret must be a JSON object"
        )
    }
  }

  private def requiredCredential(
      credentials: JsObject,
      name: String
  ): String =
    (credentials \ name)
      .asOpt[String]
      .filter(_.nonEmpty)
      .getOrElse(
        throw new IllegalStateException(
          s"Iconik credentials secret is missing $name"
        )
      )
}

case class IconikEntity(
    entityType: String,
    id: String,
    name: String,
    parentId: Option[String]
)

case class IconikCollection(title: String, parentId: Option[String])

object IconikCollection {
  implicit val reads: Reads[IconikCollection] = Reads { json =>
    for {
      title <- (json \ "title").validate[String]
      parentId <- (json \ "parent_id").validateOpt[String]
    } yield IconikCollection(title, parentId)
  }
}

case class IconikEntityValidationResult(
    entityType: String,
    id: String,
    exists: Boolean,
    nameMatches: Option[Boolean],
    parentMatches: Option[Boolean],
    expectedName: String,
    iconikName: Option[String],
    expectedParentId: Option[String],
    iconikParentId: Option[String]
) {
  def toJson: JsObject = Json.obj(
    "entityType" -> entityType,
    "id" -> id,
    "exists" -> exists,
    "nameMatches" -> optionalBoolean(nameMatches),
    "parentMatches" -> optionalBoolean(parentMatches),
    "expectedName" -> expectedName,
    "iconikName" -> optionalString(iconikName),
    "expectedParentId" -> optionalString(expectedParentId),
    "iconikParentId" -> optionalString(iconikParentId)
  )

  private def optionalBoolean(value: Option[Boolean]): JsValue =
    value.map(JsBoolean(_)).getOrElse(JsNull)

  private def optionalString(value: Option[String]): JsValue =
    value.map(JsString(_)).getOrElse(JsNull)
}

case class IconikValidationSummary(
    total: Int,
    missing: Int,
    nameMismatches: Int,
    parentMismatches: Int
) {
  def toJson: JsObject = Json.obj(
    "total" -> total,
    "missing" -> missing,
    "nameMismatches" -> nameMismatches,
    "parentMismatches" -> parentMismatches
  )
}

case class IconikValidationReport(
    entities: List[IconikEntityValidationResult],
    summary: IconikValidationSummary
) {
  def toJson: JsObject =
    Json.obj(
      "entities" -> JsArray(entities.map(_.toJson)),
      "summary" -> summary.toJson
    )
}

object IconikValidationReport {
  def fromEntities(
      entities: List[IconikEntityValidationResult]
  ): IconikValidationReport =
    IconikValidationReport(
      entities,
      IconikValidationSummary(
        total = entities.size,
        missing = entities.count(entity => !entity.exists),
        nameMismatches = entities.count(_.nameMatches.contains(false)),
        parentMismatches = entities.count(_.parentMatches.contains(false))
      )
    )
}

sealed trait IconikValidationError {
  def message: String
  def cause: Option[Throwable]
}

case class CachedEntitiesFailure(
    message: String,
    cause: Option[Throwable] = None
) extends IconikValidationError

case class IconikRequestFailure(
    message: String,
    cause: Option[Throwable] = None
) extends IconikValidationError
