package model

import org.cvogt.play.json.Jsonx
import com.gu.contentatom.thrift.{Image => ThriftImage, ImageAsset => ThriftImageAsset, ImageAssetDimensions => ThriftImageAssetDimensions}

case class ImageAssetDimensions(height: Int, width: Int) {
  def asThrift = ThriftImageAssetDimensions(height, width)
}

object ImageAssetDimensions {
  implicit val imageAssetDimensionsFormat = Jsonx.formatCaseClass[ImageAssetDimensions]

  def fromThrift(dim: ThriftImageAssetDimensions) = ImageAssetDimensions(dim.height, dim.width)
}

case class ImageAsset(mimeType: Option[String], file: String, dimensions: Option[ImageAssetDimensions], size: Option[Long]) {
  def asThrift = ThriftImageAsset(mimeType, file, dimensions.map(_.asThrift), size)
}

object ImageAsset {
  implicit val imageAssetFormat = Jsonx.formatCaseClass[ImageAsset]

  def fromThrift(imgAsset: ThriftImageAsset) = ImageAsset(imgAsset.mimeType, imgAsset.file, imgAsset.dimensions.map(ImageAssetDimensions.fromThrift), imgAsset.size)
}

case class Image(assets: List[ImageAsset], master: Option[ImageAsset], mediaId: String) {
  def asThrift = ThriftImage(assets.map(_.asThrift), master.map(_.asThrift), mediaId)
}

object Image {
  implicit val imageFormat = Jsonx.formatCaseClass[Image]

  def fromThrift(img: ThriftImage) = Image(img.assets.map(ImageAsset.fromThrift).toList, img.master.map(ImageAsset.fromThrift), img.mediaId)
}

