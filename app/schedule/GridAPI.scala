package schedule

import com.gu.media.logging.Logging
import model.CropOption
import org.apache.pekko.actor.ActorSystem
import play.api.libs.ws.WSClient

import java.util.concurrent.atomic.AtomicReference
import javax.inject._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

@Singleton
class GridAPI @Inject()(actorSystem: ActorSystem, ws: WSClient)(
    implicit ec: ExecutionContext
) extends Logging {

  actorSystem.scheduler.scheduleWithFixedDelay(
    initialDelay = 0.seconds,
    delay = 24.hours
  ) { () =>
    runJob()
  }

  private val agent = new AtomicReference[List[CropOption]](Nil)

  def getCropOptions: List[CropOption] = agent.get()

  private def runJob(): Unit = {
    ws.url(
      "https://api.media.test.dev-gutools.co.uk/configuration/crop-variations"
    ).withHttpHeaders(
      (
        "X-Gu-Media-Key",
        "media-atom-maker-vCClN4aOIL6qRRSVK42ikalzQwfLhfk2n4b4YSHw0tklq1pw"
      )
    ).get() onComplete {
      case Success(value) =>
        value.json
          .validate[List[CropOption]]
          .asOpt
          .fold(
            log.error(
              "Could not parse response from grid API for crop variations"
            )
          )(cropOptions => {
            log.info(
              "Updating crop options cache"
            )
            agent.set(cropOptions)
          })
      case Failure(exception) =>
        log.error(
          s"Error calling the grid API for crop variations ${exception.getMessage}"
        )
    }

  }
}
