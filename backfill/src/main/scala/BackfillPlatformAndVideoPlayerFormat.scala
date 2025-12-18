import com.gu.media.model.Platform.{Url, Youtube}
import com.gu.media.model.VideoPlayerFormat.{Default, Loop}
import org.joda.time.DateTime

object BackfillPlatformAndVideoPlayerFormat extends App with BackfillBase {

  override def planActions(): List[UpdateAction] = {

    val LoopStartDate = DateTime.parse("2025-07-16T00:00:00.000").getMillis

    val atomIds = api.getAtomIds()

    println(s"atoms found: ${atomIds.size}")

    val atoms = atomIds.flatMap(api.getMediaAtom)

    val atomsByPlatform = atoms.groupBy(_.platform)

    atomsByPlatform.keys.foreach { key =>
      println(s"platform $key: ${atomsByPlatform(key).size}")
    }

    val atomsByFormat = atoms.groupBy(_.videoPlayerFormat)

    atomsByFormat.keys.foreach { key =>
      println(s"videoPlayerFormat $key: ${atomsByFormat(key).size}")
    }

    // create action plan to update atoms
    atoms.flatMap { atom =>

      val updatedAtom = atom match {
        case atom if atom.platform.isEmpty && atom.videoPlayerFormat.isEmpty =>
          // base platform on active asset, or first asset, or default to YouTube
          val platform = atom.getActiveAsset()
            .map(_.platform)
            .orElse(atom.assets.headOption.map(_.platform))
            .orElse(Some(Youtube))
          val videoPlayerFormat = platform match {
            case Some(Youtube) => None
            case _ => Some(Loop)
          }
          println(s"missing platform and videoPlayerFormat ${atom.id} -> $platform, $videoPlayerFormat")
          atom.copy(platform = platform, videoPlayerFormat = videoPlayerFormat)
        case atom if atom.platform.isEmpty && atom.videoPlayerFormat.isDefined =>
          // platform must be self-hosted if videoPlayerFormat is defined
          println(s"missing platform ${atom.id} -> Url")
          atom.copy(platform = Some(Url))
        case atom if atom.platform.contains(Url) &&
          atom.videoPlayerFormat.isEmpty &&
          atom.contentChangeDetails.created.forall(_.date.getMillis < LoopStartDate) /*old*/ =>
          // default to Standard if no videoPlayerFormat and video is old
          println(s"missing videoPlayerFormat ${atom.id} -> Default")
          atom.copy(videoPlayerFormat = Some(Default))
        case atom if atom.platform.contains(Url) && atom.videoPlayerFormat.isEmpty =>
          // default to Loop if no videoPlayerFormat
          println(s"missing videoPlayerFormat ${atom.id} -> Loop")
          atom.copy(videoPlayerFormat = Some(Loop))
        case atom if atom.platform.contains(Youtube) && atom.videoPlayerFormat.isDefined =>
          // clear videoPlayerFormat for youtube
          println(s"videoPlayerFormat should not be defined ${atom.id} -> None")
          atom.copy(videoPlayerFormat = None)
        case atom => atom
      }

      if (updatedAtom != atom)
        Some(UpdateAction(atom, updatedAtom, shouldPublish(atom)))
      else
        None
    } // order oldest to newest
      .sortBy(
      _.atom.contentChangeDetails.lastModified.map(_.date.getMillis).getOrElse(0L)
    )
  }

}

