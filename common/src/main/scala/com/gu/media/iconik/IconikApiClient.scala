package com.gu.media.iconik

import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

trait IconikCollectionClient {
  def getCollection(
      entityId: String,
      credentials: IconikCredentials
  ): Future[Either[IconikRequestFailure, Option[IconikCollection]]]
}

class IconikApiClient(
    wsClient: WSClient,
    baseUrl: String = "https://app.iconik.io"
)(implicit executionContext: ExecutionContext)
    extends IconikCollectionClient {
  override def getCollection(
      entityId: String,
      credentials: IconikCredentials
  ): Future[Either[IconikRequestFailure, Option[IconikCollection]]] =
    wsClient
      .url(s"$baseUrl/API/assets/v1/collections/$entityId/")
      .addHttpHeaders(
        "App-ID" -> credentials.appId,
        "Auth-Token" -> credentials.token,
        "Accept" -> "application/json"
      )
      .get()
      .map { response =>
        response.status match {
          case NOT_FOUND => Right(None)
          case OK =>
            response.json.validate[IconikCollection].asEither match {
              case Right(collection) => Right(Some(collection))
              case Left(_) =>
                Left(
                  IconikRequestFailure(
                    s"Iconik returned an invalid collection for $entityId"
                  )
                )
            }
          case status =>
            Left(
              IconikRequestFailure(
                s"Iconik returned HTTP $status for collection $entityId"
              )
            )
        }
      }
      .recover { case NonFatal(error) =>
        Left(
          IconikRequestFailure(
            s"Failed to request Iconik collection $entityId",
            Some(error)
          )
        )
      }
}
