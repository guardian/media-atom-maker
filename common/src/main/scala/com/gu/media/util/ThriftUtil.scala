package com.gu.media.util

import com.gu.contentatom.thrift.atom.media._
import com.gu.contentatom.thrift._
import com.gu.media.util.JsonConversions._
import com.gu.media.util.MediaAtomImplicits._
import com.gu.media.youtube.YoutubeUrl
import play.api.libs.json._

import java.net.URI
import java.util.UUID.randomUUID
import scala.util.Try

object ThriftUtil {
  type ThriftResult[A] = Either[String, A]

  def getSingleParam(
      params: Map[String, Seq[String]],
      name: String
  ): Option[String] =
    params.get(name).flatMap(_.headOption)

  def parsePlatform(uri: String): ThriftResult[Platform] =
    uri match {
      case YoutubeUrl(_) => Right(Platform.Youtube)
      case Url(_)        => Right(Platform.Url)
      case _             => Left(s"Unrecognised platform in uri ($uri)")
    }

  def parseId(uri: String): ThriftResult[String] =
    uri match {
      case YoutubeUrl(id) => Right(id)
      case Url(url)       => Right(url)
      case _              => Left(s"couldn't extract id from uri ($uri)")
    }

  def parseAsset(
      uri: String,
      mimeType: Option[String],
      version: Long
  ): ThriftResult[Asset] =
    for {
      id <- parseId(uri).right
      platform <- parsePlatform(uri).right
    } yield Asset(
      id = id,
      assetType = AssetType.Video,
      version = version,
      platform = platform,
      mimeType = mimeType
    )

  def parseAssets(uris: Seq[String], version: Long): ThriftResult[List[Asset]] =
    uris.foldLeft(Right(Nil): ThriftResult[List[Asset]]) {
      (assetsEither, uri) =>
        for {
          assets <- assetsEither.right
          asset <- parseAsset(uri, mimeType = None, version).right
        } yield {
          asset :: assets
        }
    }

  def parseMetadata(metadata: Seq[String]): ThriftResult[Option[Metadata]] = {
    metadata.headOption match {
      case Some(meta) =>
        Json.parse(meta).validate[Metadata] match {
          case JsSuccess(data, _) => Right(Some(data))
          case JsError(error) =>
            Left(s"Couldn't parse Json for metadata $meta - $error")
        }
      case None => Right(None)
    }
  }

  def parseMediaAtom(
      params: Map[String, Seq[String]]
  ): ThriftResult[MediaAtom] = {
    val version = params.get("version").map(_.head.toLong).getOrElse(1L)
    val title = params.get("title").map(_.head) getOrElse "unknown"
    val category = params.get("category").map(_.head) match {
      case Some("documentary") => Category.Documentary
      case Some("explainer")   => Category.Explainer
      case Some("feature")     => Category.Feature
      case Some("hosted")      => Category.Hosted
      case _                   => Category.News
    }
    val description = params.get("description").map(_.head)
    val duration = params.get("duration").map(_.head.toLong)
    val source = params.get("source").map(_.head)
    val posterUrl = params.get("posterUrl").map(_.head).flatMap {
      case Url(url) => Some(url)
      case _        => None
    }
    for {
      assets <- parseAssets(params.getOrElse("uri", Nil), version).right
      metadata <- parseMetadata(params.getOrElse("metadata", Nil)).right
    } yield MediaAtom(
      assets = assets,
      activeVersion = Some(version),
      title = title,
      category = category,
      plutoProjectId = None,
      duration = duration,
      source = source,
      posterUrl = posterUrl,
      description = description,
      metadata = metadata
    )
  }

  def parseRequest(params: Map[String, Seq[String]]): ThriftResult[Atom] = {
    val id = getSingleParam(params, "id").getOrElse(randomUUID().toString)

    for (mediaAtom <- parseMediaAtom(params).right) yield {
      Atom(
        id = id,
        atomType = AtomType.Media,
        labels = Nil,
        defaultHtml = "",
        data = AtomData.Media(mediaAtom),
        contentChangeDetails = ContentChangeDetails(
          None,
          None,
          None,
          1L
        )
      ).updateDefaultHtml
    }
  }

}

object Url {

  def unapply(s: String): Option[String] = {
    Try(new URI(s))
      .filter { uri =>
        uri.isAbsolute && uri.getScheme == "https"
      }
      .map(_.toASCIIString)
      .toOption
  }
}
