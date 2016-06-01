package model

object MediaItemType extends Enumeration {
  val YOUTUBE = Value
}

case class MediaItem(
  id: String,
  itemType: MediaItemType.Value
)
