package com.gu.media.upload

// Structured keys (for reacting to S3 events)
case class UploadKey(folder: String, id: String) {
  override def toString = s"$folder/$id"
}

object UploadUri {
  def unapply(key: String): Option[(String, String)] = key.split("/").toList match {
    case folder :: id :: Nil => Some((folder, id))
    case _ => None
  }
}

case class UploadPartKey(folder: String, id: String, part: Int) {
  override def toString = s"$folder/$id/parts/$part"
}

object UploadPartKey {
  def unapply(key: String): Option[(String, String, Int)] = key.split("/").toList match {
    case folder :: id :: "parts" :: part :: Nil => Some((folder, id, part.toInt))
    case _ => None
  }
}
