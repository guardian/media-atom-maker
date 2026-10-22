package controllers

import com.gu.pandahmac.HMACAuthActions
import data.DataStores
import org.mockito.MockitoSugar.{mock, when}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{JsBoolean, JsNull}
import play.api.libs.ws.WSClient
import play.api.test.Helpers
import util.AWSConfig

import scala.util.Failure

class IconikValidationControllerTest extends AnyFlatSpec with Matchers {

  "controller construction" should "fail when the Iconik credentials ARN is missing" in {
    val awsConfig = mock[AWSConfig]
    when(awsConfig.getString("aws.secretsmanager.iconikCredentials"))
      .thenReturn(None)

    intercept[IllegalStateException] {
      new IconikValidationController(
        mock[HMACAuthActions],
        mock[DataStores],
        awsConfig,
        mock[WSClient],
        Helpers.stubControllerComponents()
      )
    }
  }

  it should "fail when the Iconik credentials secret cannot be retrieved" in {
    val awsConfig = mock[AWSConfig]
    val retrievalError = new RuntimeException("secret lookup failed")
    when(awsConfig.getString("aws.secretsmanager.iconikCredentials"))
      .thenReturn(Some("secret-arn"))
    when(awsConfig.getSecret("secret-arn"))
      .thenReturn(Failure(retrievalError))

    val exception = intercept[IllegalStateException] {
      new IconikValidationController(
        mock[HMACAuthActions],
        mock[DataStores],
        awsConfig,
        mock[WSClient],
        Helpers.stubControllerComponents()
      )
    }

    exception.getCause shouldBe retrievalError
  }

  "parentMatches" should "return unknown when the expected parent is unavailable" in {
    IconikValidationController.parentMatches(
      None,
      Some("iconik-parent")
    ) shouldBe JsNull
  }

  it should "compare the Iconik parent with a known expected parent" in {
    IconikValidationController.parentMatches(
      Some("expected-parent"),
      Some("expected-parent")
    ) shouldBe JsBoolean(true)

    IconikValidationController.parentMatches(
      Some("expected-parent"),
      Some("different-parent")
    ) shouldBe JsBoolean(false)
  }
}
