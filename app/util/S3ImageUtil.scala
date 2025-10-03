package util

import com.amazonaws.services.s3.model.S3Object
import com.gu.media.logging.Logging
import com.gu.media.model.{Image, ImageAsset, ImageAssetDimensions}

import java.io.InputStream
import javax.imageio.{ImageIO, ImageReader}
import scala.util.{Failure, Success, Try}

class S3ImageUtil(awsConfig: AWSConfig) extends Logging {

  /**
   * creates an Image instance for an image file stored in S3 that is available to the web via
   * a guardian host e.g. https://uploads.guim.co.uk/{image-key}
   *
   * @param s3Bucket   Name of the S3 bucket
   * @param imageUrl   The url through which the image is available on the web
   *                   e.g. https://uploads.guim.co.uk/my-image
   * @return An Option containing the Image populated with a single asset and the master field
   *         None if the image can't be found or read from S3
   */
  def getS3Image(s3Bucket: String, imageUrl: String): Option[Image] = {
    // assume that the url is made of an http origin and the s3 key
    val s3Url = """(https://.*?)/(.*)""".r

    imageUrl match {

      case s3Url(httpOrigin, s3Key) =>
        val s3ImageAsset = getS3ImageAsset(s3Bucket, s3Key, httpOrigin)
        s3ImageAsset.map { asset =>
          Image(
            assets = List(asset),
            master = Some(asset),
            mediaId = "",
            source = None
          )
        }

      case _ => None
    }
  }

  /**
   * creates an ImageAsset for an image file stored in S3 that is available to the web via
   * a guardian host e.g. https://uploads.guim.co.uk/{image-key}
   *
   * [The initial application is to provide an image of the first frame of video in a media atom.
   * MediaConvert produces a jpeg file and this is saved as the posterImage in the atom.]
   *
   * @param s3Bucket   Name of the S3 bucket
   * @param s3Key      Key for the image object in the bucket
   * @param httpOrigin The protocol and host through which the object is available on the web
   *                   e.g. https://uploads.guim.co.uk
   * @return An Option containing the ImageAsset populated with dimension and size metadata
   *         None if the image can't be found or read
   */
  def getS3ImageAsset(s3Bucket: String, s3Key: String, httpOrigin: String): Option[ImageAsset] = {

    val s3Client = awsConfig.s3Client

    Try {
      val obj = s3Client.getObject(s3Bucket, s3Key)
      val metadata = obj.getObjectMetadata
      val contentType = metadata.getContentType

      ImageAsset(
        mimeType = Some(contentType),
        file = s"$httpOrigin/$s3Key",
        dimensions = readImageDimensions(obj, contentType),
        size = Some(metadata.getContentLength),
        aspectRatio = None)

    } match {
      case Success(img) =>
        Some(img)
      case Failure(ex) =>
        log.error(s"Failed to read image from S3: ${ex.getMessage}")
        None
    }
  }

  def getImageReader(contentType: String): Option[ImageReader] = {
    val iter = ImageIO.getImageReadersByMIMEType(contentType)
    if (iter.hasNext) {
      Some(iter.next())
    } else {
      log.warn(s"No image reader for $contentType")
      None
    }
  }

  def readDimensionsFromStream(reader: ImageReader, content: InputStream): ImageAssetDimensions = {
    val stream = ImageIO.createImageInputStream(content)
    reader.setInput(stream)
    val width = reader.getWidth(reader.getMinIndex)
    val height = reader.getHeight(reader.getMinIndex)
    log.info(s"Image has width $width, height $height")
    ImageAssetDimensions(height, width)
  }

  def readImageDimensions(imgFile: S3Object, contentType: String): Option[ImageAssetDimensions] = {
    getImageReader(contentType).flatMap { reader =>
      val content = imgFile.getObjectContent
      val tryDimensions = Try(readDimensionsFromStream(reader, content))
      content.close()
      reader.dispose()
      tryDimensions match {
        case Success(dimensions) =>
          Some(dimensions)
        case Failure(ex) =>
          log.warn(s"Error reading dimensions from ${imgFile.getKey}", ex);
          None
      }
    }
  }
}

object S3ImageUtil {
  def imageHasUrl(image: Image, imageUrl: String): Boolean = {
    val filename = imageUrl.split('/').last
    image.assets.exists(_.file.endsWith(filename))
  }
}
