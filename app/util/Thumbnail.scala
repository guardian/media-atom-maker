package util

import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io._
import java.net.URL

import com.google.api.client.http.InputStreamContent
import com.gu.media.model.{Image, ImageAsset}
import javax.imageio.ImageIO
import play.api.Environment

object Thumbnail {
  // YouTube have a file size limit of 2MB
  // see https://developers.google.com/youtube/v3/docs/thumbnails/set
  // use a slightly smaller file from Grid so we can add a branding overlay
  private val MAX_SIZE = 1.8 * 1000 * 1000

  // amount of padding (px) on left and bottom of logo
  private val PADDING = 50

  private def getGridImageAsset(image: Image): ImageAsset = {
    image.assets
      .filter(asset => asset.size.nonEmpty && asset.size.get < MAX_SIZE)
      .maxBy(_.size.get)
  }

  private def imageAssetToBufferedImage(imageAsset: ImageAsset): BufferedImage =
    ImageIO.read(new URL(imageAsset.file))

  private def getLogo(env: Environment): BufferedImage =
    ImageIO.read(env.getFile(s"conf/logo.png"))

  private def overlayImages(bgImage: BufferedImage, logo: BufferedImage): ByteArrayInputStream = {
    val logoWidth = List(bgImage.getWidth() / 3, logo.getWidth()).min
    val logoHeight = logo.getHeight() / (logo.getWidth() / logoWidth)

    val logoX = PADDING
    val logoY = bgImage.getHeight() - logoHeight - PADDING

    val graphics = bgImage.createGraphics
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    graphics.drawImage(bgImage, 0, 0, null)
    graphics.drawImage(logo, logoX, logoY, logoWidth, logoHeight, null)
    graphics.dispose()

    val os = new ByteArrayOutputStream()
    ImageIO.write(bgImage, "jpg", os)
    new ByteArrayInputStream(os.toByteArray)
  }

  def getVideoThumbnail(image: Image, env: Environment): InputStreamContent = {
    val imageAsset = getGridImageAsset(image)
    val gridImage = imageAssetToBufferedImage(imageAsset)
    val watermark = getLogo(env)

    new InputStreamContent(
      imageAsset.mimeType.get,
      new BufferedInputStream(overlayImages(gridImage, watermark))
    )
  }
}
