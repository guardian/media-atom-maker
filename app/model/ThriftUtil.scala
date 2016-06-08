package model

import com.gu.contentatom.thrift._
import atom.media._
import java.util.UUID.randomUUID
import play.api.mvc.{ BodyParser, BodyParsers }
import scala.concurrent.ExecutionContext

object ThriftUtil {
  type ThriftResult[A] = Either[String, A]

  implicit class AtomDataWithType(a: Atom) {
    def dataAs[D <: AtomData : Manifest]: D = a.data.asInstanceOf[D]

    def mediaData = dataAs[AtomData.Media].media

    def updateDataAs[D <: AtomData : Manifest](f: D => D): Atom = a.copy(data = f(a.dataAs[D]))

    def updateMediaData(f: MediaAtom => MediaAtom) =
      updateDataAs[AtomData.Media](atomData => atomData.copy(media = f(atomData.media)))

    def withRevision(f: Long => Long): Atom = a.copy(
      contentChangeDetails = a.contentChangeDetails.copy(
        revision = f(a.contentChangeDetails.revision)
      )
    )
    def withRevision(newRevision: Long): Atom = withRevision(_ => newRevision)
  }

  val youtube = "https?://www.youtube.com/watch\\?v=([^&]+)".r

  def getSingleParam(params: Map[String, Seq[String]], name: String): Option[String] =
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

  def parseAsset(uri: String, version: Long): ThriftResult[Asset] =
    for {
      id <- parseId(uri).right
      platform <- parsePlatform(uri).right
    } yield Asset(
      id = id,
      assetType = AssetType.Video,
      version = version,
      platform = platform
    )

  def parseAssets(uris: Seq[String], version: Long): ThriftResult[List[Asset]] =
    uris.foldLeft(Right(Nil): ThriftResult[List[Asset]]) { (assetsEither, uri) =>
      for {
        assets <- assetsEither.right
        asset <- parseAsset(uri, version).right
      } yield {
        asset :: assets
      }
    }

  def parseMediaAtom(params: Map[String, Seq[String]]): ThriftResult[MediaAtom] = {
    val version = params.get("version").map(_.head.toLong).getOrElse(1L)
    for {
      assets <- parseAssets(
        params.get("uri").getOrElse(Nil),
        version
      ).right
    } yield MediaAtom(
      assets = assets,
      activeVersion = version,
      plutoProjectId = None
    )
  }

  def parseRequest(params: Map[String, Seq[String]]): ThriftResult[Atom] = {
    val id = getSingleParam(params,"id").getOrElse(randomUUID().toString)

    for(mediaAtom <- parseMediaAtom(params).right) yield Atom(
      id = id,
      atomType = AtomType.Media,
      labels = Nil,
      defaultHtml = "<div></div>",
      data = AtomData.Media(mediaAtom),
      contentChangeDetails = ContentChangeDetails(
        None, None, None, 1L
      )
    )
  }

  def getSingleRequiredParam(params: Map[String, Seq[String]], name: String): ThriftResult[String] =
    getSingleParam(params, name).toRight(s"Missing param ${name}")

  def atomBodyParser(implicit ec: ExecutionContext): BodyParser[ThriftResult[Atom]] =
    BodyParsers.parse.urlFormEncoded map { urlParams =>
      parseRequest(urlParams)
    }

  def assetBodyParser(implicit ec: ExecutionContext): BodyParser[ThriftResult[Asset]] =
    BodyParsers.parse.urlFormEncoded map { urlParams =>
      for {
        uri <- getSingleRequiredParam(urlParams, "uri").right
        version <- getSingleRequiredParam(urlParams, "version").right
        asset <- parseAsset(uri, version.toLong).right
      } yield asset
    }
}
