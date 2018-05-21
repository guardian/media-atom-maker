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
  // get a slightly smaller file from Grid, so we can add a branding overlay
  private val MAX_SIZE = 1.8 * 1000 * 1000

  private def getGridImage(imageAsset: ImageAsset): BufferedImage =
    ImageIO.read(new URL(imageAsset.file))

  private def getWatermark(env: Environment): BufferedImage =
    ImageIO.read(env.getFile(s"conf/logo.png"))

  private def overlayImages(bgImage: BufferedImage, fgImage: BufferedImage): ByteArrayInputStream = {
    val graphics = bgImage.createGraphics
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    graphics.drawImage(bgImage, 0, 0, null)

    val padding = 50
    val maxWidth = List(bgImage.getWidth() / 3, fgImage.getWidth()).min
    val maxHeight = fgImage.getHeight() / (fgImage.getWidth() / maxWidth)

    graphics.drawImage(fgImage, padding, bgImage.getHeight() - maxHeight - padding, maxWidth, maxHeight, null)

    graphics.dispose()

    val os = new ByteArrayOutputStream()
    ImageIO.write(bgImage, "jpg", os)
    new ByteArrayInputStream(os.toByteArray)
  }

  def getVideoThumbnail(image: Image, env: Environment): InputStreamContent = {
    val imageAsset = image.assets
      .filter(asset => asset.size.nonEmpty && asset.size.get < MAX_SIZE)
      .maxBy(_.size.get)

    val gridImage = getGridImage(imageAsset)
    val watermark = getWatermark(env)

    new InputStreamContent(
      imageAsset.mimeType.get,
      new BufferedInputStream(overlayImages(gridImage, watermark))
    )
  }
}
