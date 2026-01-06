import com.gu.media.model.{MediaAtom, Platform, VideoPlayerFormat}
import com.gu.media.model.Platform.{Url, Youtube}
import com.gu.media.model.VideoPlayerFormat.{Default, Loop}
import org.joda.time.DateTime

object BackfillPlatformAndVideoPlayerFormat extends App with BackfillBase {

  // Date when looping videos were introduced to MAM
  val LoopStartDate = DateTime.parse("2025-07-16T00:00:00.000").getMillis

  override def planActions(): List[UpdateAction] = {

    println("fetching atom id's...")

    val atomIds = api.getAtomIds(limit = 50000)

    println(s"atoms found: ${atomIds.size}")

    println("fetching atoms...")

    val atoms = atomIds.zipWithIndex.flatMap { case (id, i) =>
      print(s"\r${"% 6d".format(i + 1)}")
      api.getMediaAtom(id)
    }

    println()

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

        val (reason, updatedAtom) = atom match {
          case atom
              if atom.platform.isEmpty && atom.videoPlayerFormat.isEmpty =>

            val platform = derivePlatform(atom)
            val videoPlayerFormat = deriveVideoPlayerFormat(atom, platform)

            s"missing platform and videoPlayerFormat ${atom.id} -> $platform, $videoPlayerFormat" ->
              atom.copy(
                platform = platform,
                videoPlayerFormat = videoPlayerFormat
              )

          case atom
              if atom.platform.isEmpty && atom.videoPlayerFormat.isDefined =>
            // platform must be self-hosted if videoPlayerFormat is defined
            s"missing platform ${atom.id} -> Url" ->
              atom.copy(platform = Some(Url))

          case atom
              if atom.platform.contains(Url) &&
                atom.videoPlayerFormat.isEmpty =>

            val videoPlayerFormat = deriveVideoPlayerFormat(atom, atom.platform)

            s"missing videoPlayerFormat ${atom.id} -> $videoPlayerFormat" ->
              atom.copy(videoPlayerFormat = Some(Default))

          case atom
              if atom.platform.contains(Youtube) &&
                atom.videoPlayerFormat.isDefined =>
            // clear videoPlayerFormat for youtube
            s"videoPlayerFormat should not be defined ${atom.id} -> None" ->
              atom.copy(videoPlayerFormat = None)

          case atom => "" -> atom
        }

        if (updatedAtom != atom) {
          println(reason)
          Some(UpdateAction(atom, updatedAtom, shouldPublish(atom), reason))
        } else
          None
      } // order oldest to newest
      .sortBy(
        _.atom.contentChangeDetails.lastModified
          .map(_.date.getMillis)
          .getOrElse(0L)
      )
  }

  def derivePlatform(atom: MediaAtom): Option[Platform] = {
    // base platform on active asset, or first asset, or default to YouTube
    atom
      .getActiveAsset()
      .map(_.platform)
      .orElse(atom.assets.headOption.map(_.platform))
      .orElse(Some(Youtube))
  }

  def deriveVideoPlayerFormat(atom: MediaAtom, platform: Option[Platform]): Option[VideoPlayerFormat] = {
    platform match {
      case Some(Youtube) =>
        // youtube doesn't have a video player format
        None
      case Some(Url) if atom.contentChangeDetails.created.forall(_.date.getMillis < LoopStartDate) =>
        // self-hosted videos before loops were introduced should be of type Default
        Some(Default)
      case _             =>
        // otherwise it's a loop
        Some(Loop)
    }
  }
}
