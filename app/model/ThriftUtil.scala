package model

import com.gu.contentatom.thrift._
import atom.media._
import java.util.UUID.randomUUID
import play.api.mvc.{ BodyParser, BodyParsers }
import scala.concurrent.ExecutionContext

class ThriftUtil(params: Map[String, String]) {
  import ThriftUtil.ThriftResult

  val youtube = "https?://www.youtube.com/watch\\?v=([^&]+)".r

  def getParam(paramName: String): ThriftResult[String] =
    params.get(paramName).toRight(s"Missing parameter $paramName")

  def parsePlatform: ThriftResult[Platform] = getParam("uri").right flatMap { uri =>
    uri match {
      case youtube(_) => Right(Platform.Youtube)
      case _ => Left(s"Unrecognised platform in uri ($uri)")
    }
  }

  def parseId: ThriftResult[String] = getParam("uri").right flatMap { uri =>
    uri match {
      case youtube(id) => Right(id)
      case _ => Left(s"couldn't extract id from uri ($uri)")
    }
  }

  def parseVersion: Long = params.get("version").map(_.toLong).getOrElse(1L)

  def parseAsset: ThriftResult[Asset] =
    for {
      id <- parseId.right
      platform <- parsePlatform.right
    } yield Asset(
      id = id,
      assetType = AssetType.Video,
      version = params.get("version").getOrElse("1").toLong,
      platform = platform
    )

  def parseMediaAtom: ThriftResult[MediaAtom] = {
    for(asset <- parseAsset.right) yield MediaAtom(
      assets = List(asset),
      activeVersion = parseVersion,
      plutoProjectId = None
    )
  }

  def parseRequest: ThriftResult[Atom] =
    for(mediaAtom <- parseMediaAtom.right) yield Atom(
      id = params.get("id").getOrElse(randomUUID().toString),
      atomType = AtomType.Media,
      labels = Nil,
      defaultHtml = "<div></div>",
      data = AtomData.Media(mediaAtom),
      contentChangeDetails = ContentChangeDetails(
        None, None, None, 1L
      )
    )
}

object ThriftUtil {
  type ThriftResult[A] = Either[String, A]

  def bodyParser(implicit ec: ExecutionContext): BodyParser[ThriftResult[Atom]] =
    BodyParsers.parse.urlFormEncoded map { urlParams =>
      new ThriftUtil(urlParams.mapValues(_.head)).parseRequest
    }
}
