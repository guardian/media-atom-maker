package util

import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.PublishRequest
import com.gu.atom.publish.AtomPublisher
import com.gu.contentatom.thrift.{ContentAtomEvent, EventType}
import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.{Json, Writes}

import scala.util.Try

class NotifyingAtomPublisher(isLive: Boolean, topicArn: String, underlying: AtomPublisher, sns: AmazonSNS) extends AtomPublisher {
  override def publishAtomEvent(event: ContentAtomEvent): Try[Unit] = {
    underlying.publishAtomEvent(event).map { _ =>
      val notification = SimpleContentUpdate.fromEvent(event, isLive)
      val json = Json.stringify(Json.toJson(notification))

      // TODO MRB: should we have a new message-type called atom update?
      val request = new PublishRequest(topicArn, json, "composer-update")
      Try(sns.publish(request))
    }
  }
}

case class SimpleContentUpdate( composerId: String,
                                whatChanged: String,
                                eventTime: DateTime,
                                revision: Option[Long],
                                isLive: Boolean,
                                isMigration: Boolean = false,
                                isProdmon: Boolean,
                                isExpired: Boolean
                              )

object SimpleContentUpdate {
  implicit val writes: Writes[SimpleContentUpdate] = Json.writes[SimpleContentUpdate]

  def fromEvent(event: ContentAtomEvent, isLive: Boolean): SimpleContentUpdate = SimpleContentUpdate(
    composerId = s"${event.atom.atomType.toString}-${event.atom.id}",
    whatChanged = if(event.eventType == EventType.Takedown) { "takeDown" } else { "update" },
    eventTime = new DateTime(event.eventCreationTime, DateTimeZone.UTC),
    revision = Some(event.atom.contentChangeDetails.revision),
    isLive,
    isMigration = false,
    isProdmon = false,
    isExpired = false
  )
}
