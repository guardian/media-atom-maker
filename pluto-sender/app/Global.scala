import com.gu.media.logging.Logging
import play.api.GlobalSettings
import util.AWSConfig
import play.api.{Application, GlobalSettings}
import lib.uploaderMessageConsumer


object Global extends GlobalSettings {

  override def beforeStart(app: Application): Unit = {

    //stop consumer
  }

  override def onStop(app: Application): Unit = {

    //start consumer
  }

}

