import com.google.inject.AbstractModule
import com.google.inject.Stage.DEVELOPMENT
import com.gu.pandomainauth.action.AuthActions

class Module extends AbstractModule {
  def configure() = {
    if (currentStage() == DEVELOPMENT) {
      bind(classOf[AuthActions]).to(classOf[controllers.DevAuthActions])
    } else {
      bind(classOf[AuthActions]).to(classOf[controllers.PanDomainAuthActions])
    }
  }
}
