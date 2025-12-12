import com.gu.media.model.AssetType.{Subtitles, Video}
import com.gu.media.model.MediaAtom
import com.gu.media.model.Platform.Url
import com.gu.media.util.AspectRatio

import scala.annotation.tailrec
import scala.io.StdIn.readLine

case class UpdateAction(atom: MediaAtom, updatedAtom: MediaAtom, shouldPublish: Boolean)

object BackfillDimensions extends App {

  println("\nVideo Atom Backfiller uses the MAM API to update and re-publish existing atoms that have missing data")
  println("For authentication you have to grab some Request Headers from the browser:")
  println("- Go to the chosen MAM environment e.g. https://video.gutools.co.uk or https://video.code.dev-gutools.co.uk")
  println("- Go into a video")
  println("- Open and clear the network tab in the dev tools")
  println("- Make a minor change to the furniture headline or standfirst")
  println("- Observe the PUT request from apiRequest.ts")
  println("- Export the Cookie header to a MAM_COOKIE environment variable")
  println("- Export the Csrf-Token header to a MAM_CSRF_TOKEN environment variable")
  println("Run this app in the same shell using `sbt backfill/run`")

  val baseUrl = chooseBaseUrl()
  val cookie = inputOrEnv("Paste Cookie header from Media Atom Maker", "MAM_COOKIE")
  val csrfToken = inputOrEnv("Paste Crsf-Token header from Media Atom Maker", "MAM_CRSF_TOKEN")

  val http = new Http("Cookie" -> cookie, "Csrf-Token" -> csrfToken)
  val api = new AtomMakerApi(http, baseUrl)
  val dimensionsHelper = new DimensionsHelper(http)

  val atomIds = api.getAtomIdsForPlatform("url")

  // create action plan to update atoms
  val plan = atomIds.flatMap { atomId =>
    api.getMediaAtom(atomId).flatMap { atom =>

      // fix any assets that have issue
      val updatedAssets = atom.assets.map {

        case asset if asset.mimeType.contains("text/vtt") && asset.assetType != Subtitles =>
          println(s"wrong asset type for VTT $atomId: ${asset.assetType} -> Subtitles")
          asset.copy(assetType = Subtitles)

        case asset if asset.assetType == Video && asset.platform == Url && asset.dimensions.isEmpty =>
          val dims = dimensionsHelper.readDimensions(asset)
          val aspRatio = dims.flatMap(dim => AspectRatio.calculate(dim.width, dim.height)).map(_.name)
          println(s"missing dimensions $atomId: None, None -> $dims, $aspRatio")
          asset.copy(dimensions = dims, aspectRatio = aspRatio)

        case asset if asset.assetType == Video && asset.platform == Url && asset.aspectRatio.isEmpty =>
          val aspRatio = asset.dimensions.flatMap(dim => AspectRatio.calculate(dim.width, dim.height)).map(_.name)
          println(s"missing aspect ratio $atomId: None -> $aspRatio")
          asset.copy(aspectRatio = aspRatio)

        case asset => asset
      }

      if (updatedAssets != atom.assets)
        Some(UpdateAction(atom, atom.copy(assets = updatedAssets), shouldPublish(atom)))
      else
        None
    }
  } // order oldest to newest
    .sortBy(
      _.atom.contentChangeDetails.lastModified.map(_.date.getMillis).getOrElse(0L)
    )

  println("-----------")

  chooseActions(plan)
    .foreach { action =>
      println("\nChanges ->")
      println(action.atom)
      println(action.updatedAtom)

      println(s"Update ->\n${api.updateMediaAtom(action.updatedAtom)}")
      if (action.shouldPublish) {
        println(s"Publish ->\n${api.publishMediaAtom(action.atom.id)}")
      }
    }

  /* if we update this atom, does it need re-publishing?
   *
   * - atom is not published => don't publish
   * - the current version of the atom has been published => re-publish
   * - an earlier version of the atom has been published => don't publish
   */
  def shouldPublish(atom: MediaAtom): Boolean = {
    println(s"current revision ${atom.contentChangeDetails.revision}")
    atom.contentChangeDetails.published match {
      case Some(_) =>
        api.getPublishedMediaAtom(atom.id).exists { publishedAtom =>
          println(s"published revision ${publishedAtom.contentChangeDetails.revision}")
          publishedAtom.contentChangeDetails.revision == atom.contentChangeDetails.revision
        }
      case None =>
        false
    }
  }

  @tailrec
  def chooseBaseUrl(): String =
    readLine("Are you backfilling CODE (C), or PROD (P): ").trim match {
      case "C" => "https://video.code.dev-gutools.co.uk"
      case "P" => "https://video.gutools.co.uk"
      case _ => chooseBaseUrl()
    }

  @tailrec
  def chooseActions(plan: Seq[UpdateAction]): Seq[UpdateAction] =
    readLine(s"There are ${plan.size} action(s). Execute all (A), one (1) or exit (X): ").trim match {
      case "A" => plan
      case "1" => plan.take(1)
      case "X" => Nil
      case _ => chooseActions(plan)
    }

  def inputOrEnv(prompt: String, envName: String): String = {
    readLine(s"$prompt (or Enter to use $envName env var): ").trim match {
      case "" => scala.sys.env(envName)
      case value => value
    }
  }
}

