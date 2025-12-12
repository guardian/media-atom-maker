import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Metadata

import java.io.OutputStreamWriter
import java.net.{HttpURLConnection, URI}
import scala.io.Source
import scala.util.{Failure, Success, Using}

class Http(headers: (String, String)*) {

  val headerMap: Map[String, String] = headers.toMap

  def getConnection(url: String): HttpURLConnection = {
    val connection = URI.create(url).toURL.openConnection.asInstanceOf[HttpURLConnection]
    connection.setRequestProperty("Cookie", headerMap("Cookie"))
    connection
  }

  def get(url: String): Option[String] =
    Using(getConnection(url).getInputStream) { inputStream =>
      Source.fromInputStream(inputStream).getLines.mkString("\n")
    } match {
      case Success(value) => Some(value)
      case Failure(e) => println(s"Error reading $url: ${e.getMessage}"); None
    }

  def getVideoMetadata(url: String): Option[Metadata] =
    Using(getConnection(url).getInputStream) { inputStream =>
      ImageMetadataReader.readMetadata(inputStream)
    } match {
      case Success(value) => Some(value)
      case Failure(e) => println(s"Error reading $url: ${e.getMessage}"); None
    }

  def put(url: String, content: String): Option[String] = {
    val connection = getConnection(url)
    connection.setDoOutput(true)
    connection.setRequestMethod("PUT")
    connection.setRequestProperty("Csrf-Token", headerMap("Csrf-Token"))
    connection.setRequestProperty("Content-Type","application/json")
    Using(new OutputStreamWriter(connection.getOutputStream)) { out =>
      out.write(content)
    } match {
      case Success(_) =>
        Using(connection.getInputStream) { inputStream =>
          Source.fromInputStream(inputStream).getLines.mkString("\n")
        } match {
          case Success(value) => Some(value)
          case Failure(e) => println(s"Error reading response from $url: ${e.getMessage}"); None
        }
      case Failure(e) => println(s"Error putting to $url: ${e.getMessage}"); None
    }
  }


}
