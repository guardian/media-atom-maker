import com.google.inject.AbstractModule
import com.google.inject.Stage.DEVELOPMENT
import com.gu.atom.publish.AtomPublisher
import com.gu.pandomainauth.action.AuthActions
import data.AtomPublisherProvider

class Module extends AbstractModule {
  def configure() = {
    
    if (currentStage() == DEVELOPMENT) {
      bind(classOf[AuthActions])
      .to(classOf[controllers.DevAuthActions])
    } else {
      bind(classOf[AuthActions])
      .to(classOf[controllers.PanDomainAuthActions])
    }

    bind(classOf[AtomPublisher])
    .toProvider(classOf[AtomPublisherProvider])
  }
}
