package model

import ai.x.play.json.Encoders._
import ai.x.play.json.Jsonx
import play.api.libs.json.Format

case class WorkflowMediaAtom(title: String)
object WorkflowMediaAtom {
  implicit val userFormat: Format[WorkflowMediaAtom] = Jsonx.formatCaseClassUseDefaults[WorkflowMediaAtom]
}
