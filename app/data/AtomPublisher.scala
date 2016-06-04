package data

import com.google.inject.ImplementedBy
import com.gu.contentatom.thrift.ContentAtomEvent
import scala.util.Try

@ImplementedBy(classOf[KinesisAtomPublisher])
trait AtomPublisher {
  def publishAtomEvent(event: ContentAtomEvent): Try[Unit]
}
