import com.google.inject.AbstractModule
import com.gu.pandomainauth.action.AuthActions

class Module extends AbstractModule {
  def configure() = {
    bind(classOf[AuthActions])
    .to(classOf[controllers.PanDomainAuthActions])
  }
}
