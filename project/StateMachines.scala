import sbt._
import sbt.Keys._

object StateMachines {
  case class LambdaConfig(description: String, timeout: Int = 60)

  val lambdas = settingKey[Map[String, LambdaConfig]]("The lambdas to include in the state machine")

  val compileTemplate = Def.task {
    val template = IO.read((resourceDirectory in Compile).value / "cfn-template.yaml")
    val lambdaTemplate = IO.read((resourceDirectory in Compile).value / "lambda-template.yaml")
    val stateMachine = IO.read((resourceDirectory in Compile).value / "state-machine.json")

    val withLambdas = (lambdas in Compile).value.foldLeft(template) {
      case (tmpl, (name, LambdaConfig(description, timeout))) =>
        val instance = lambdaTemplate
          .replace("{{name}}", name)
          .replace("{{description}}", description)
          .replace("{{timeout}}", timeout.toString)

        replace(s"{{$name}}", instance, tmpl)
    }

    val compiled = replace("{{state_machine}}", stateMachine, withLambdas)
    val output = (resourceManaged in Compile).value / "media-atom-pipeline.yaml"

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
