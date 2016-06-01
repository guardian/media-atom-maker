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

  type ThriftResult[A] = Either[String, A]

  val youtube = "https?://www.youtube.com/watch?v=([^&]?)".r

  def getParam(params: Map[String, String], paramName: String): ThriftResult[String] =
    params.get(paramName).toRight(s"Missing parameter $paramName")

  def parsePlatform(uri: String): ThriftResult[Platform] = uri match {
    case youtube(_) => Right(Platform.Youtube)
    case _ => Left(s"Unrecognised platform in uri ($uri)")
  }

  def parseId(uri: String): ThriftResult[String] = uri match {
    case youtube(id) => Right(id)
    case _ => Left(s"couldn't extract id from uri ($uri)")
  }

  def parseVersion(params: Map[String, String]): Long =
    params.get("version").map(_.toLong).getOrElse(1L)

  def parseAsset(params: Map[String, String]): ThriftResult[Asset] =
    for {
      uri <- getParam(params, "uri").right
      id <- parseId(uri).right
      platform <- parsePlatform(uri).right
    } yield Asset(
      id = id,
      assetType = AssetType.Video,
      version = params.get("version").getOrElse("1").toLong,
      platform = platform
    )

  def parseMediaAtom(params: Map[String, String]): ThriftResult[MediaAtom] = {
    for(asset <- parseAsset(params).right) yield MediaAtom(
      assets = List(asset),
      activeVersion = parseVersion(params),
      plutoProjectId = None
    )
  }

  def parseRequest(params: Map[String, String]): ThriftResult[Atom] =
    for(mediaAtom <- parseMediaAtom(params).right) yield Atom(
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
