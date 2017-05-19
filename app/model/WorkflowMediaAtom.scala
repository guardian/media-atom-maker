package model

import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

case class WorkflowMediaAtom(title: String)
object WorkflowMediaAtom {
  implicit val userFormat: Format[WorkflowMediaAtom] = Jsonx.formatCaseClassUseDefaults[WorkflowMediaAtom]
}
