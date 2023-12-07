package util

import java.awt.RenderingHints
import java.awt.image.{AffineTransformOp, BufferedImage, ColorConvertOp}
import java.io._
import java.net.URL
import com.google.api.client.http.InputStreamContent
import com.gu.media.logging.Logging
import com.gu.media.model.{Image, ImageAsset}

import java.awt.geom.AffineTransform
import javax.imageio.ImageIO

case class ThumbnailGenerator(logoFile: File) extends Logging {
  private lazy val logo = ImageIO.read(logoFile)

  private def getGridImageAsset(image: Image): ImageAsset =
    image.master.getOrElse(
      image.assets
        .filter(asset => asset.size.nonEmpty)
        .maxBy(_.size.get)
    )

  private def imageAssetToBufferedImage(imageAsset: ImageAsset): BufferedImage = {
    val image = ImageIO.read(new URL(imageAsset.file))
    val rgbImage = new BufferedImage(image.getWidth, image.getHeight, BufferedImage.TYPE_3BYTE_BGR)
    new ColorConvertOp(null).filter(image, rgbImage)
  }

  private def overlayImages(atomId: String)(bgImage: BufferedImage): BufferedImage = {
    val logoWidth: Double = List(bgImage.getWidth() / 3.0, logo.getWidth()).min
    val logoHeight: Double = logo.getHeight() / (logo.getWidth() / logoWidth)

    // amount of padding (px) on left and bottom of logo
    val PADDING = (bgImage.getHeight() / 18.0).toInt
    val logoX = PADDING
    val logoY = bgImage.getHeight() - logoHeight.toInt - PADDING

    val graphics = bgImage.createGraphics
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    graphics.drawImage(bgImage, 0, 0, null)
    log.info(
      s"Creating branded thumbnail for atom $atomId. Image dims ${bgImage.getWidth()} x ${bgImage.getHeight()}. Logo dims ${logoWidth.toInt} x ${logoHeight.toInt} (x:$logoX, y:$logoY)"
    )
    graphics.drawImage(logo, logoX, logoY, logoWidth.toInt, logoHeight.toInt, null)
    graphics.dispose()

    bgImage
  }

  private def rescaleImage(atomId: String)(image: BufferedImage): BufferedImage = {
    val originalWidth = image.getWidth
    val originalHeight = image.getHeight
    val portrait = originalHeight > originalWidth
    val landscape = !portrait
    val reasonableSize = 2560.toDouble

    if ((landscape && originalWidth <= reasonableSize) || (portrait && originalHeight <= reasonableSize)) {
      // image is a reasonable size, return without scaling
      image
    } else {
      // image is oversized; resize down to 2560 on longest side
      val scale = reasonableSize / (if (portrait) originalHeight else originalWidth).toDouble
      log.info(s"Scaling thumbnail for atom $atomId. From $originalWidth x $originalHeight by $scale")
      val transform = AffineTransform.getScaleInstance(scale, scale)
      val op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR)

      val width = (originalWidth * scale).toInt
      val height = (originalHeight * scale).toInt

      val dest = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR)
      op.filter(image, dest)
    }
  }

  private def streamImage(image: BufferedImage, mimeType: String): ByteArrayInputStream = {
    val os = new ByteArrayOutputStream(500000)

    // Although this list isn't exhaustive of all image types,
    // it is exhaustive of the current formats supported by Grid.
    // NB: Grid only returns mime-type as image/* for the master crop,
    // smaller crop renditions are just `jpg` or `png`.
    // TODO find a better way to convert mime-type to ImageIO write format
    val imageIOWriteFormat = mimeType match {
      case "image/png" | "png" => "png"
      case _ => "jpg"
    }

    ImageIO.write(image, imageIOWriteFormat, os)
    new ByteArrayInputStream(os.toByteArray)
  }

  private def processImage(image: Image, ops: (BufferedImage => BufferedImage)*): InputStreamContent = {
    val imageAsset = getGridImageAsset(image)
    val gridImage = imageAssetToBufferedImage(imageAsset)
    val mimeType = imageAsset.mimeType.getOrElse("image/jpeg")

    val finalImage = Function.chain(ops).apply(gridImage)

    new InputStreamContent(
      mimeType,
      new BufferedInputStream(streamImage(finalImage, mimeType))
    )
  }

  def getBrandedThumbnail(image: Image, atomId: String): InputStreamContent =
    processImage(image, overlayImages(atomId), rescaleImage(atomId))

  def getThumbnail(image: Image, atomId: String): InputStreamContent =
    processImage(image, rescaleImage(atomId))
}
