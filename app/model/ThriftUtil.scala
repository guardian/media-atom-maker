package model

import com.gu.contentatom.thrift._
import atom.media._
import java.util.UUID.randomUUID
import play.api.mvc.{ BodyParser, BodyParsers }
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext

class ThriftUtil(params: Map[String, Seq[String]]) {
  import ThriftUtil.ThriftResult

  val youtube = "https?://www.youtube.com/watch\\?v=([^&]+)".r

  private lazy val uri = params.get("uri")

  def getSingleParam(name: String): Option[String] =
    params.get(name).flatMap(_.headOption)

  def parsePlatform(uri: String): ThriftResult[Platform] =
    uri match {
      case youtube(_) => Right(Platform.Youtube)
      case _ => Left(s"Unrecognised platform in uri ($uri)")
    }

  def parseId(uri: String): ThriftResult[String] =
    uri match {
      case youtube(id) => Right(id)
      case _ => Left(s"couldn't extract id from uri ($uri)")
    }

  def parseVersion: Long =
    getSingleParam("version").map(_.toLong).getOrElse(1L)

  def parseAsset(uri: String): ThriftResult[Asset] =
    for {
      id <- parseId(uri).right
      platform <- parsePlatform(uri).right
    } yield Asset(
      id = id,
      assetType = AssetType.Video,
      version = params.get("version").flatMap(_.headOption).getOrElse("1").toLong,
      platform = platform
    )

  def parseAssets: ThriftResult[List[Asset]] =
    (params.get("uri").map { uris =>
      parseAsset(uris.head).right.map(_ :: Nil)
    }) getOrElse Right(Nil)

  def parseMediaAtom: ThriftResult[MediaAtom] =
    for(assets <- parseAssets.right) yield MediaAtom(
      assets = assets,
      activeVersion = parseVersion,
      plutoProjectId = None
    )

  def parseRequest: ThriftResult[Atom] =
    for(mediaAtom <- parseMediaAtom.right) yield Atom(
      id = getSingleParam("id").getOrElse(randomUUID().toString),
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
      new ThriftUtil(urlParams).parseRequest
    }
}
