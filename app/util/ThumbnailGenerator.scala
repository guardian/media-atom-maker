package util

import java.awt.RenderingHints
import java.awt.image.{BufferedImage, ColorConvertOp}
import java.io._
import java.net.URL
import com.google.api.client.http.InputStreamContent
import com.gu.media.logging.Logging
import com.gu.media.model.{Image, ImageAsset}

import javax.imageio.ImageIO

case class ThumbnailGenerator(logoFile: File) extends Logging {
  // YouTube have a file size limit of 2MB
  // see https://developers.google.com/youtube/v3/docs/thumbnails/set
  // use a slightly smaller file from Grid so we can add a branding overlay
  private val MAX_SIZE = 1.7 * 1000 * 1000

  private lazy val logo = ImageIO.read(logoFile)

  private def getGridImageAsset(image: Image): ImageAsset = {
    image.assets
      .filter(asset => asset.size.nonEmpty && asset.size.get < MAX_SIZE)
      .maxBy(_.size.get)
  }

  private def imageAssetToBufferedImage(
      imageAsset: ImageAsset
  ): BufferedImage = {
    val image = ImageIO.read(new URL(imageAsset.file))
    val rgbImage = new BufferedImage(
      image.getWidth,
      image.getHeight,
      BufferedImage.TYPE_3BYTE_BGR
    )
    new ColorConvertOp(null).filter(image, rgbImage)
  }

  private def overlayImages(
      bgImage: BufferedImage,
      bgImageMimeType: String,
      atomId: String
  ): ByteArrayInputStream = {
    val logoWidth: Double = List(bgImage.getWidth() / 3.0, logo.getWidth()).min
    val logoHeight: Double = logo.getHeight() / (logo.getWidth() / logoWidth)

    // amount of padding (px) on left and bottom of logo
    val PADDING = (bgImage.getHeight() / 18.0).toInt
    val logoX = PADDING
    val logoY = bgImage.getHeight() - logoHeight.toInt - PADDING

    val graphics = bgImage.createGraphics
    graphics.setRenderingHint(
      RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON
    )
    graphics.drawImage(bgImage, 0, 0, null)
    log.info(
      s"Creating branded thumbnail for atom $atomId. Image dims ${bgImage.getWidth()} x ${bgImage.getHeight()}. Logo dims ${logoWidth.toInt} x ${logoHeight.toInt} (x:$logoX, y:$logoY)"
    )
    graphics.drawImage(
      logo,
      logoX,
      logoY,
      logoWidth.toInt,
      logoHeight.toInt,
      null
    )
    graphics.dispose()

    val os = new ByteArrayOutputStream()

    // Although this list isn't exhaustive of all image types,
    // it is exhaustive of the current formats supported by Grid.
    // NB: Grid only returns mime-type as image/* for the master crop,
    // smaller crop renditions are just `jpg` or `png`.
    // TODO find a better way to convert mime-type to ImageIO write format
    val imageIOWriteFormat = bgImageMimeType match {
      case "image/png" | "png" => "png"
      case _                   => "jpg"
    }

    ImageIO.write(bgImage, imageIOWriteFormat, os)
    new ByteArrayInputStream(os.toByteArray)
  }

  def getBrandedThumbnail(image: Image, atomId: String): InputStreamContent = {
    val imageAsset = getGridImageAsset(image)
    val gridImage = imageAssetToBufferedImage(imageAsset)
    val mimeType = imageAsset.mimeType.getOrElse("image/jpeg")

    new InputStreamContent(
      mimeType,
      new BufferedInputStream(overlayImages(gridImage, mimeType, atomId))
    )
  }

  def getThumbnail(image: Image): InputStreamContent = {
    val imageAsset = getGridImageAsset(image)
    val mimeType = imageAsset.mimeType.getOrElse("image/jpeg")
    val url = new URL(imageAsset.file)

    new InputStreamContent(
      mimeType,
      new BufferedInputStream(url.openStream())
    )
  }
}
