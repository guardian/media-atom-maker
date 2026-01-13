import com.gu.media.model.AssetType.Video
import com.gu.media.model.Platform.Url
import com.gu.media.model.{Asset, MediaAtom, Platform}

import scala.annotation.tailrec
import scala.io.StdIn.readLine

case class UpdateAction(atom: MediaAtom, updatedAtom: MediaAtom, shouldPublish: Boolean, reason: String = "")

trait BackfillBase {

  println(
    "\nVideo Atom Backfiller uses the MAM API to update and re-publish existing atoms that have missing data"
  )
  println(
    "For authentication you have to grab some Request Headers from the browser:"
  )
  println(
    "- Go to the chosen MAM environment e.g. https://video.gutools.co.uk or https://video.code.dev-gutools.co.uk"
  )
  println("- Go into a video")
  println("- Open and clear the Network tab in the dev tools")
  println("- Make a minor change to the furniture headline or standfirst")
  println("- Observe the PUT request from apiRequest.ts in the Network tab")
  println(
    "- Export the Cookie request header to a MAM_COOKIE environment variable"
  )
  println(
    "- Export the Csrf-Token request header to a MAM_CSRF_TOKEN environment variable"
  )
  println("Run this app in the same shell using `sbt \"backfiller/runMain BackfillXXXXX\"`")

  val baseUrl = chooseBaseUrl()
  val cookie =
    inputOrEnv("Paste Cookie header from Media Atom Maker", "MAM_COOKIE")
  val csrfToken = inputOrEnv(
    "Paste Csrf-Token header from Media Atom Maker",
    "MAM_CSRF_TOKEN"
  )

  val http = new Http("Cookie" -> cookie, "Csrf-Token" -> csrfToken)
  val api = new AtomMakerApi(http, baseUrl)

  val plan = planActions()

  println("\n-----------")

  applyActions(plan)

  /**
   * make a list of backfill actions that need to be performed on individual atoms
   * @return
   */
  def planActions(): List[UpdateAction]

  /**
   * perform the planned actions
   * @param plan
   */
  def applyActions(plan: List[UpdateAction]): Unit =
    chooseActions(plan)
      .foreach { action =>
        println(action.reason)
        println("\nChanges ->")
        println(action.atom)
        println(action.updatedAtom)

        println(s"Update ->\n${api.updateMediaAtom(action.updatedAtom)}")
        if (action.shouldPublish) {
          println(s"Publish ->\n${api.publishMediaAtom(action.atom.id)}")
        }
      }



  def inputOrEnv(prompt: String, envName: String): String = {
    readLine(s"$prompt (or Enter to use $envName env var): ").trim match {
      case ""    => scala.sys.env(envName)
      case value => value
    }
  }

  @tailrec
  final def chooseBaseUrl(): String =
    readLine("Are you backfilling CODE (C), or PROD (P): ").trim match {
      case "C" => "https://video.code.dev-gutools.co.uk"
      case "P" => "https://video.gutools.co.uk"
      case _   => chooseBaseUrl()
    }

  @tailrec
  final def chooseActions(plan: Seq[UpdateAction]): Seq[UpdateAction] =
    readLine(
      s"There are ${plan.size} action(s). Execute all (A), first N (e.g. 1000), or exit (X): "
    ).trim match {
      case "A" => plan
      case n if n.matches("\\d+") => plan.take(n.toInt)
      case "X" => Nil
      case _   => chooseActions(plan)
    }

  /*
   * If atom is hosted on Youtube, don't re-publish, as this might wipe YouTube metadata for older videos.
   *
   * If atom is self-hosted:
   * - atom is not published => don't re-publish
   * - the current version of the atom has been published => re-publish
   * - an earlier version of the atom has been published => don't re-publish
   */
  def shouldPublish(atom: MediaAtom, platform: Option[Platform]): Boolean = {
      platform.contains(Url) &&
      atom.contentChangeDetails.published.isDefined &&
      api.getPublishedMediaAtom(atom.id).exists(_.contentChangeDetails.revision == atom.contentChangeDetails.revision)
  }
}
