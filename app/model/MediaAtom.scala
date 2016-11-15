package model

import org.cvogt.play.json.Jsonx

import com.gu.contentatom.thrift.{
Atom => ThriftAtom,
AtomType => ThriftAtomType,
AtomData
}

import com.gu.contentatom.thrift.atom.media.{
MediaAtom => ThriftMediaAtom,
Metadata => ThriftMetadata
}

import _root_.util.atom.MediaAtomImplicits

// Note: This is *NOT* structured like the thrift representation
case class MediaAtom(
  // Atom wrapper fields
  id: String,
  labels: List[String],
  changeDetails: ContentChangeDetails,
  // data field
  assets: List[Asset],
  activeVersion: Option[Long],
  title: String,
  category: Category,
  plutoProjectId: Option[String],
  duration: Option[Long],
  source: Option[String],
  posterUrl: Option[String],
  description: Option[String],
  // metadata
  tags: Option[List[String]],
  categoryId: Option[String],
  license: Option[String],
  commentsEnabled: Option[Boolean],
  channelId: Option[String]
  ) extends MediaAtomImplicits {

  def asThrift = ThriftAtom(
      id = id,
      atomType = ThriftAtomType.Media,
      labels = List(),
      defaultHtml = generateHtml(),
      data = ThriftMediaAtom(
        assets = assets.map(_.asThrift),
        activeVersion = activeVersion,
        title = title,
        category = category.asThrift,
        plutoProjectId = plutoProjectId,
        duration = duration,
        source = source,
        posterUrl = posterUrl,
        description = description,
        metadata = Some(ThriftMetadata(
          tags = tags,
          categoryId = categoryId,
          license = license,
          commentsEnabled = commentsEnabled,
          channelId = channelId
          ))
        ).asInstanceOf[AtomData],
      contentChangeDetails = changeDetails.asThrift,
      flags = None
  )

  private def generateHtml(): String = {
    "<div></div>"
  }
}

object MediaAtom {
  implicit val mediaAtomFormat = Jsonx.formatCaseClass[MediaAtom]

  def fromThrift(atom: ThriftAtom) = {
    val data = atom.data.asInstanceOf[ThriftMediaAtom]

    MediaAtom(
      id = atom.id,
      labels = atom.labels.toList,
      changeDetails = ContentChangeDetails.fromThrift(atom.contentChangeDetails),
      assets = data.assets.map(Asset.fromThrift).toList,
      activeVersion = data.activeVersion,
      title = data.title,
      category = Category.fromThrift(data.category),
      plutoProjectId = data.plutoProjectId,
      duration = data.duration,
      source = data.source,
      posterUrl = data.posterUrl,
      description = data.description,
      tags = data.metadata.flatMap(_.tags.map(_.toList)),
      categoryId = data.metadata.flatMap(_.categoryId),
      license = data.metadata.flatMap(_.license),
      commentsEnabled = data.metadata.flatMap(_.commentsEnabled),
      channelId = data.metadata.flatMap(_.channelId)
    )
  }
}
