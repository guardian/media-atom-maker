package com.gu.media.lambda

import java.io.{InputStream, OutputStream}
import com.amazonaws.services.lambda.runtime.{Context, RequestStreamHandler}
import com.google.common.base.Charsets
import com.gu.media.config.{Code, Stage}
import com.gu.media.telemetry.Telemetry
import play.api.libs.json.{Json, Reads, Writes}
import play.libs.ws.WSClient

import java.net.http.HttpClient

abstract class LambdaWithParams[I: Reads, O: Writes]
    extends RequestStreamHandler {
  def handle(input: I, telemetry: Telemetry): O

  override def handleRequest(
      rawInput: InputStream,
      rawOutput: OutputStream,
      context: Context
  ): Unit = {
    val input = Json.parse(rawInput).as[I]
    val client = HttpClient.newHttpClient()
    val secretArn =
      "arn:aws:secretsmanager:eu-west-1:563563610310:secret:/CODE/flexible/event-api-lambda/hmacSecret-OVcnV0"
    val telemetry = new Telemetry(Code, secretArn, client)

    val output = handle(input, telemetry)

    val outputStr = Json.stringify(Json.toJson(output))
    rawOutput.write(outputStr.getBytes(Charsets.UTF_8))
    rawOutput.flush()
  }
}
