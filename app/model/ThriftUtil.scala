package model

import com.gu.contentatom.thrift._
import atom.media._
import java.util.UUID.randomUUID
import play.api.mvc.{ BodyParser, BodyParsers }
import scala.concurrent.ExecutionContext

trait AtomDataTyper[D] {
  def getData(a: Atom): D
  def setData(a: Atom, newData: D): Atom
}

trait AtomImplicits[D] {
  val dataTyper: AtomDataTyper[D]
  implicit class AtomWithData(atom: Atom) {
    def tdata = dataTyper.getData(atom)
    def withData(data: D): Atom = dataTyper.setData(atom, data)
    def updateData(f: D => D): Atom = dataTyper.setData(atom, f(atom.tdata))
    def withRevision(f: Long => Long): Atom = atom.copy(
      contentChangeDetails = atom.contentChangeDetails.copy(
        revision = f(atom.contentChangeDetails.revision)
      )
    )
    def withRevision(newRevision: Long): Atom = withRevision(_ => newRevision)
  }
}

trait MediaAtomImplicits extends AtomImplicits[MediaAtom] {
  val dataTyper = new AtomDataTyper[MediaAtom] {
    def getData(a: Atom) = a.data.asInstanceOf[AtomData.Media].media
    def setData(a: Atom, newData: MediaAtom) =
      a.copy(data = a.data.asInstanceOf[AtomData.Media].copy(media = newData))
  }
}

object MediaAtomImplicits extends MediaAtomImplicits

object ThriftUtil {
  type ThriftResult[A] = Either[String, A]

  // implicit val mediaAtomDefaultHtml = new DefaultHTMLGenerator[AtomData.Media] {
  //   def makeDefaultHtml(data: AtomData.Media) = {
  //     data.media.assets
  //       .map(asset => views.html.MediaAtom.embedAsset(asset))
  //       .mkString("\n")
  //   }
  // }

  // implicit class AtomDataWithType(a: Atom) {
  //   def dataAs[D <: AtomData : Manifest]: D = a.data.asInstanceOf[D]

  //   def mediaData = dataAs[AtomData.Media].media

  //   def updateDataAs[D <: AtomData : Manifest](f: D => D): Atom = a.copy(data = f(a.dataAs[D]))

  //   def updateMediaData(f: MediaAtom => MediaAtom) =
  //     updateDataAs[AtomData.Media](atomData => atomData.copy(media = f(atomData.media)))

  //   def withRevision(f: Long => Long): Atom = a.copy(
  //     contentChangeDetails = a.contentChangeDetails.copy(
  //       revision = f(a.contentChangeDetails.revision)
  //     )
  //   )
  //   def withRevision(newRevision: Long): Atom = withRevision(_ => newRevision)
  // }

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

    for(mediaAtom <- parseMediaAtom(params).right) yield {
      Atom(
        id = id,
        atomType = AtomType.Media,
        labels = Nil,
        defaultHtml = "",
        data = AtomData.Media(mediaAtom),
        contentChangeDetails = ContentChangeDetails(
          None, None, None, 1L
        )
      )
    }
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
