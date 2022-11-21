import sbt._
import sbt.Keys._

object StateMachine {
  case class LambdaConfig(description: String, timeout: Int = 300)

  val lambdas = settingKey[Map[String, LambdaConfig]]("The lambdas to include in the state machine")

  val compileTemplate = Def.task {
    val template = IO.read((Compile / resourceDirectory).value / "cfn-template.yaml")
    val lambdaTemplate = IO.read((Compile / resourceDirectory).value / "lambda-template.yaml")
    val stateMachine = IO.read((Compile / resourceDirectory).value / "state-machine.json")

    val withLambdas = (Compile / lambdas).value.foldLeft(template) {
      case (tmpl, (name, LambdaConfig(description, timeout))) =>
        val instance = lambdaTemplate
          .replace("{{name}}", name)
          .replace("{{description}}", description)
          .replace("{{timeout}}", timeout.toString)

        replace(s"{{$name}}", instance, tmpl)
    }

    val compiled = replace("{{state_machine}}", stateMachine, withLambdas)
    val output = (Compile / resourceManaged).value / "media-atom-pipeline.yaml"

    IO.write(output, compiled)

    Seq(output)
  }

  private def replace(find: String, replace: String, content: String): String = {
    content.split("\n").flatMap {
      case line if line.trim() == find =>
        val tabbing = line.takeWhile { c => c != '{' }
        replace.split("\n").map(tabbing + _)
      case line =>
        List(line.replace(find, replace))
    }.mkString("\n")
  }
}
