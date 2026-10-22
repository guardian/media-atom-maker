package controllers

import com.gu.media.iconik.{
  IconikApiClient,
  IconikCollection,
  IconikCredentials,
  IconikRequestFailure
}
import org.mockito.MockitoSugar.{mock, when}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

class IconikApiClientTest extends AnyFlatSpec with Matchers {
  private implicit val executionContext: ExecutionContext =
    ExecutionContext.global

  "IconikApiClient" should "interpret successful, missing, and failed responses" in {
    val wsClient = mock[WSClient]
    val credentials = IconikCredentials("test-app", "test-token")
    val existingResponse = mock[WSResponse]
    val missingResponse = mock[WSResponse]
    val failedResponse = mock[WSResponse]

    def stubResponse(
        entityId: String,
        response: WSResponse,
        status: Int
    ): Unit = {
      val request = mock[WSRequest]
      when(
        wsClient.url(
          s"https://app.iconik.io/API/assets/v1/collections/$entityId/"
        )
      ).thenReturn(request)
      when(
        request.addHttpHeaders(
          "App-ID" -> credentials.appId,
          "Auth-Token" -> credentials.token,
          "Accept" -> "application/json"
        )
      ).thenReturn(request)
      when(request.get()).thenReturn(Future.successful(response))
      when(response.status).thenReturn(status)
    }

    stubResponse("existing", existingResponse, 200)
    stubResponse("missing", missingResponse, 404)
    stubResponse("failed", failedResponse, 500)
    when(existingResponse.json)
      .thenReturn(Json.obj("title" -> "Collection", "parent_id" -> "parent"))

    val client = new IconikApiClient(wsClient)

    Await.result(client.getCollection("existing", credentials), 5.seconds) shouldBe
      Right(Some(IconikCollection("Collection", Some("parent"))))
    Await.result(client.getCollection("missing", credentials), 5.seconds) shouldBe
      Right(None)
    Await.result(client.getCollection("failed", credentials), 5.seconds) shouldBe
      Left(IconikRequestFailure("Iconik returned HTTP 500 for collection failed"))
  }
}
