import com.google.inject.AbstractModule
import com.gu.atom.data._
import com.gu.atom.publish._
import data._
import com.gu.pandahmac.HMACAuthActions
import util.{LogShipping, LogShippingImpl}


class Module extends AbstractModule {
  def configure() = {

    bind(classOf[LogShipping])
      .to(classOf[LogShippingImpl]).asEagerSingleton()

    bind(classOf[HMACAuthActions])
      .to(classOf[controllers.PanDomainAuthActions])

    bind(classOf[PublishedDataStore])
    .toProvider(classOf[PublishedMediaAtomDataStoreProvider])

    bind(classOf[PreviewDataStore])
      .toProvider(classOf[PreviewMediaAtomDataStoreProvider])

    bind(classOf[LiveAtomPublisher])
    .toProvider(classOf[LiveAtomPublisherProvider])

    bind(classOf[PreviewAtomPublisher])
      .toProvider(classOf[PreviewAtomPublisherProvider])

    bind(classOf[PreviewAtomReindexer])
      .toProvider(classOf[PreviewAtomReindexerProvider])

    bind(classOf[PublishedAtomReindexer])
      .toProvider(classOf[PublishedAtomReindexerProvider])

    bind(classOf[AuditDataStore])
      .toProvider(classOf[AuditDataStoreProvider])
  }
}
