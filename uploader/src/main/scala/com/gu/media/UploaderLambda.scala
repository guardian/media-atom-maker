package com.gu.media

import com.gu.media.UploaderLambda.{Input, Output}
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

class UploaderLambda extends LambdaWithParams[Input, Output] with Logging {
  override def handleRequest(input: Input): Output = {
    val youTube = YouTubeClient(config)
    val output = s"allowedChannels: ${youTube.allowedChannels.mkString(",")}. result: ${input.string * input.integer}"

    log.info(output)

    Output(output)
  }
}

object UploaderLambda {
  case class Input(string: String, integer: Int)
  case class Output(result: String)

  implicit val inputFormat: Format[Input] = Jsonx.formatCaseClass[Input]
  implicit val outputFormat: Format[Output] = Jsonx.formatCaseClass[Output]
}

object UploaderLambdaApp extends App {
  val lambda = new UploaderLambda
  println(lambda.handleRequest(Input("hello world!", 4)))
}
