import com.google.inject.AbstractModule
import com.gu.atom.data.{ MediaAtomDataStoreProvider, DataStore }
import com.gu.atom.publish._
import com.gu.pandomainauth.action.AuthActions
import data._

class Module extends AbstractModule {
  def configure() = {
    bind(classOf[AuthActions])
      .to(classOf[controllers.PanDomainAuthActions])

    bind(classOf[DataStore])
    .toProvider(classOf[MediaAtomDataStoreProvider])

    bind(classOf[LiveAtomPublisher])
    .toProvider(classOf[LiveAtomPublisherProvider])

    bind(classOf[PreviewAtomPublisher])
      .toProvider(classOf[PreviewAtomPublisherProvider])

    bind(classOf[PreviewAtomReindexer])
      .toProvider(classOf[PreviewAtomReindexerProvider])

    bind(classOf[PublishedAtomReindexer])
      .toProvider(classOf[PublishedAtomReindexerProvider])
  }
}
