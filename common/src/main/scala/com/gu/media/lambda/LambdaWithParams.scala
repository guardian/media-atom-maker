package com.gu.media.lambda

import java.io.{InputStream, OutputStream}

import com.amazonaws.services.lambda.runtime.{Context, RequestStreamHandler}
import com.google.common.base.Charsets
import play.api.libs.json.{Json, Reads, Writes}

abstract class LambdaWithParams[I: Reads, O: Writes]
    extends RequestStreamHandler
    with LambdaBase {
  def handle(input: I): O

  override def handleRequest(
      rawInput: InputStream,
      rawOutput: OutputStream,
      context: Context
  ): Unit = {
    val input = Json.parse(rawInput).as[I]
    val output = handle(input)

    val outputStr = Json.stringify(Json.toJson(output))
    rawOutput.write(outputStr.getBytes(Charsets.UTF_8))
    rawOutput.flush()
  }
}
