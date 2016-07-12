import com.google.inject.AbstractModule
import com.gu.atom.publish.AtomPublisher
import com.gu.pandomainauth.action.AuthActions
import data.AtomPublisherProvider

class Module extends AbstractModule {
  def configure() = {
    bind(classOf[AuthActions])
    .to(classOf[controllers.PanDomainAuthActions])

    bind(classOf[AtomPublisher])
    .toProvider(classOf[AtomPublisherProvider])
  }
}
