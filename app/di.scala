import com.google.inject.AbstractModule
import com.gu.atom.publish.{LiveAtomPublisher, PreviewAtomPublisher}
import com.gu.pandomainauth.action.AuthActions
import data.{PreviewAtomPublisherProvider, LiveAtomPublisherProvider}

class Module extends AbstractModule {
  def configure() = {
    bind(classOf[AuthActions])
    .to(classOf[controllers.PanDomainAuthActions])

    bind(classOf[LiveAtomPublisher])
    .toProvider(classOf[LiveAtomPublisherProvider])

    bind(classOf[PreviewAtomPublisher])
      .toProvider(classOf[PreviewAtomPublisherProvider])
  }
}
