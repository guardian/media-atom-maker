import sbt._
import sbt.Keys._

object StateMachine {
  sealed trait Architecture {
    val name: String
  }
  object Architecture {
    object x64 extends Architecture {
      override val name: String = "x86_64"
    }

    object arm64 extends Architecture {
      override val name: String = "arm64"
    }
  }
  case class LambdaConfig(
    description: String,
    timeout: Int = 300,
    memory: Int = 2048,
    architecture: Architecture = Architecture.arm64,
  )

  val lambdas = settingKey[Map[String, LambdaConfig]]("The lambdas to include in the state machine")

  val compileTemplate = Def.task {
    val template = IO.read((Compile / resourceDirectory).value / "cfn-template.yaml")
    val lambdaTemplate = IO.read((Compile / resourceDirectory).value / "lambda-template.yaml")
    val stateMachine = IO.read((Compile / resourceDirectory).value / "state-machine.json")

    val withLambdas = (Compile / lambdas).value.foldLeft(template) {
      case (tmpl, (name, LambdaConfig(description, timeout, memory, architecture))) =>
        val instance = lambdaTemplate
          .replace("{{name}}", name)
          .replace("{{description}}", description)
          .replace("{{timeout}}", timeout.toString)
          .replace("{{memory}}", memory.toString)
          .replace("{{architecture}}", architecture.name)

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
