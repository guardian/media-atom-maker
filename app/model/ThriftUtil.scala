package model

import com.gu.contentatom.thrift._
import play.api.libs.json._
import java.util.UUID.randomUUID
import com.gu.contentatom.thrift.atom.media._
import play.api.mvc.{ BodyParser, BodyParsers }

object ThriftUtil {

//   val bodyParser: BodyParser[Atom] = BodyParsers.parse.urlFormEncoded map { params =>
//     params.mapValues(_.head)
//   }
//   val data = js.value
//   val platform = parsePlatform(data("uri").as[String])
// }

  val youtube = "https?://www.youtube.com/watch?v=([^&]?)".r

  def parsePlatform(uri: String): Option[Platform] = uri match {
    case youtube(_) => Some(Platform.Youtube)
    case _ => None
  }

  def parseId(uri: String): Option[String] = uri match {
    case youtube(id) => Some(id)
    case _ => None
  }

  def parseVersion(params: Map[String, String]): Long =
    params.get("version").map(_.toLong).getOrElse(1L)

  def parseAsset(params: Map[String, String]): Option[Asset] =
    for {
      uri <- params.get("uri")
      id <- parseId(uri)
      platform <- parsePlatform(uri)
    } yield Asset(
      id = id,
      assetType = AssetType.Video,
      version = params.get("version").getOrElse("1").toLong,
      platform = platform
    )

  def parseMediaAtom(params: Map[String, String]): Option[MediaAtom] = {
    for(asset <- parseAsset(params)) yield MediaAtom(
      assets = List(asset),
      activeVersion = parseVersion(params),
      plutoProjectId = None
    )
  }

  def parseRequest(params: Map[String, String]): Option[Atom] =
    for(mediaAtom <- parseMediaAtom(params)) yield Atom(
      id = randomUUID().toString,
      atomType = AtomType.Media,
      labels = Nil,
      defaultHtml = "<div></div>",
      data = AtomData.Media(mediaAtom),
      contentChangeDetails = ContentChangeDetails(
        None, None, None, 1L
      )
    )
}
