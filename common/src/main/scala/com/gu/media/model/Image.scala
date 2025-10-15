package com.gu.media.model

import com.gu.contentatom.thrift.{
  Image => ThriftImage,
  ImageAsset => ThriftImageAsset,
  ImageAssetDimensions => ThriftImageAssetDimensions
}
import com.gu.ai.x.play.json.Encoders._
import com.gu.ai.x.play.json.Jsonx
import play.api.libs.json.OFormat

case class ImageAssetDimensions(height: Int, width: Int) {
  def asThrift = ThriftImageAssetDimensions(height, width)
}

object ImageAssetDimensions {
  implicit val imageAssetDimensionsFormat: OFormat[ImageAssetDimensions] =
    Jsonx.formatCaseClass[ImageAssetDimensions]

  def fromThrift(dim: ThriftImageAssetDimensions) =
    ImageAssetDimensions(dim.height, dim.width)
}

case class ImageAsset(
    mimeType: Option[String],
    file: String,
    dimensions: Option[ImageAssetDimensions],
    size: Option[Long],
    aspectRatio: Option[String]
) {
  def asThrift = ThriftImageAsset(
    mimeType,
    file,
    dimensions.map(_.asThrift),
    size,
    aspectRatio
  )
}

object ImageAsset {
  implicit val imageAssetFormat: OFormat[ImageAsset] =
    Jsonx.formatCaseClass[ImageAsset]

  def fromThrift(imgAsset: ThriftImageAsset) = ImageAsset(
    imgAsset.mimeType,
    imgAsset.file,
    imgAsset.dimensions.map(ImageAssetDimensions.fromThrift),
    imgAsset.size,
    imgAsset.aspectRatio
  )
}

case class Image(
    assets: List[ImageAsset],
    master: Option[ImageAsset],
    mediaId: String,
    source: Option[String]
) {
  def asThrift =
    ThriftImage(assets.map(_.asThrift), master.map(_.asThrift), mediaId, source)
}

object Image {
  implicit val imageFormat: OFormat[Image] = Jsonx.formatCaseClass[Image]

  def fromThrift(img: ThriftImage) =
    Image(
      img.assets.map(ImageAsset.fromThrift).toList,
      img.master.map(ImageAsset.fromThrift),
      img.mediaId,
      img.source
    )
}
