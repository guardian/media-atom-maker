package util

import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.PublishRequest
import com.gu.atom.publish.AtomPublisher
import com.gu.contentatom.thrift.{ContentAtomEvent, EventType}
import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.{Json, Writes}
import play.api.libs.json.JodaWrites._
import play.api.libs.json.JodaReads._

import scala.util.Try

class NotifyingAtomPublisher(
    isLive: Boolean,
    topicArn: String,
    underlying: AtomPublisher,
    sns: AmazonSNS
) extends AtomPublisher {
  override def publishAtomEvent(event: ContentAtomEvent): Try[Unit] = {
    underlying.publishAtomEvent(event).flatMap { _ =>
      val notification = SimpleContentUpdate.fromEvent(event, isLive)
      val json = Json.stringify(Json.toJson(notification))

      val request = new PublishRequest(topicArn, json, "atom-update")
      Try(sns.publish(request))
    }
  }
}

case class SimpleContentUpdate(
    id: String,
    whatChanged: String,
    eventTime: DateTime,
    revision: Option[Long],
    isLive: Boolean,
    isMigration: Boolean = false,
    isProdmon: Boolean,
    isExpired: Boolean
)

object SimpleContentUpdate {
  implicit val writes: Writes[SimpleContentUpdate] =
    Json.writes[SimpleContentUpdate]

  def fromEvent(event: ContentAtomEvent, isLive: Boolean): SimpleContentUpdate =
    SimpleContentUpdate(
      id = s"${event.atom.atomType.toString}/${event.atom.id}",
      whatChanged = if (event.eventType == EventType.Takedown) { "takeDown" }
      else { "update" },
      eventTime = new DateTime(event.eventCreationTime, DateTimeZone.UTC),
      revision = Some(event.atom.contentChangeDetails.revision),
      isLive,
      isMigration = false,
      isProdmon = false,
      isExpired = false
    )
}
