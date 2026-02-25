import com.gu.media.model.{Asset, ImageAssetDimensions}
import scala.jdk.CollectionConverters._

class DimensionsHelper(http: Http) {

  def getDimensionTag(tags: Map[String, String], tagName: String): Option[Int] =
    // e.g. Width - 1280 pixels
    for {
      value <- tags.get(tagName)
      str <- value.split(" ").headOption
      int <- str.toIntOption
    } yield int


  def readDimensions(asset: Asset): Option[ImageAssetDimensions] = {
    asset.mimeType match {
      case Some("application/vnd.apple.mpegurl") =>
        // read resolution from m3u8 file
        val dimensionsRegex = """.*RESOLUTION=(\d+)x(\d+).*""".r  // Width x Height
        http.get(asset.id).flatMap { content =>
          // note that m3u8 file can contain multiple resolutions, but for purposes of backfilling, we only expect one
          // #EXT-X-STREAM-INF:BANDWIDTH=1461136,AVERAGE-BANDWIDTH=1421229,CODECS="avc1.64001e,mp4a.40.2",RESOLUTION=406x720,FRAME-RATE=25.000,SUBTITLES="subs"
          content.linesIterator.collectFirst {
            case dimensionsRegex(w, h) => ImageAssetDimensions(h.toInt, w.toInt)
          }
        }
      case Some("video/mp4") =>
        // read metadata from mp4
        val tags = for {
          metadata <- http.getVideoMetadata(asset.id).toList
          dir <- metadata.getDirectories.asScala
          tag <- dir.getTags.asScala
        } yield (tag.getTagName, tag.getDescription)
        val tagMap = tags.toMap
        getDimensionTag(tagMap, "Width") -> getDimensionTag(tagMap, "Height") match {
          case (Some(width), Some(height)) => Some(ImageAssetDimensions(height, width))
          case _ => None
        }
      case _ => None
    }
  }


}
