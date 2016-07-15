import com.google.inject.AbstractModule
import com.gu.atom.publish.{ AtomPublisher, AtomReindexer }
import com.gu.pandomainauth.action.AuthActions
import data._

class Module extends AbstractModule {
  def configure() = {
    bind(classOf[AuthActions])
      .to(classOf[controllers.PanDomainAuthActions])

    bind(classOf[AtomPublisher])
      .toProvider(classOf[AtomPublisherProvider])

    bind(classOf[AtomReindexer])
      .toProvider(classOf[AtomReindexerProvider])
  }
}
