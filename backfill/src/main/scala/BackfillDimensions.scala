import com.gu.media.model.AssetType.{Subtitles, Video}
import com.gu.media.model.MediaAtom
import com.gu.media.model.Platform.Url

case class UpdateAction(atom: MediaAtom, updatedAtom: MediaAtom)

object BackfillDimensions extends App {

  val cookie = "consentUUID=f592651d-f09d-4231-95c6-26678ec0a2f3_50; consentDate=2025-11-26T10:14:03.070Z; PLAY_SESSION=eyJhbGciOiJIUzI1NiJ9.eyJkYXRhIjp7ImNzcmZUb2tlbiI6IjQxZGQ5ZGI0NDAxYmQ2NGFkNzJiOTFlNTg1MTMyYmMyNTBkMTY1NDMtMTc2NDg2NDk3OTMxMC1lNmUzOWIwOTE4NmM4NDE2MDUyNzBhNjIifSwibmJmIjoxNzY0ODY0OTc5LCJpYXQiOjE3NjQ4NjQ5Nzl9.mofILz7kyHeD41WSZ3hlWHUUh-iBL9yU1n-ZZRjFYUY; gutoolsAuth-assym=Zmlyc3ROYW1lPUFuZHkmbGFzdE5hbWU9Tm90b24mZW1haWw9YW5keS5ub3Rvbi5jb250cmFjdG9yQGd1YXJkaWFuLmNvLnVrJmF2YXRhclVybD1odHRwczovL2xoMy5nb29nbGV1c2VyY29udGVudC5jb20vYS9BQ2c4b2NMdjZ2OEl0d05lc3dkNFRrMFNKUlBJdkhXcGpMN243QmdpOHVyQ1hmb1ctQzJrUVE9czk2LWMmc3lzdGVtPXZpZGVvJmF1dGhlZEluPWZyb250cyx2aWV3ZXIsdmlkZW8sd29ya2Zsb3cmZXhwaXJlcz0xNzY1MjgzOTM0MDAwJm11bHRpZmFjdG9yPXRydWU=.EiFGLrz/E2T83bKa99C6Eih7BUSpKB9sjDoDo14AQdGQpeYu76tEI1B4f4gfRqM14E2SNxyv4apZvqgmLw7L8N2ZZ8TuvGnbwyADna7dOaZD4e3PqWClnaXoNLvKAvAAq092CScjtWfdIfwazzUoz69RRTJXzeNTXtFq35/aKYA51R4uJ7TMRniIYs4v4bolqb9oqO9o9tGS+qNMltUTVosBloKQfurwbbQi76lrvHWDb6mQhY5G+p52TYKysjrBg6/Uqgn9gwRiaFd81pXLAkqtqYxJ0p6lar81IdRpiqxVR5awephJNxpwProUEOpHW7J2ovyzEcY0MvY5RFLk06SVMAIQ2TxtH4cycjlEK3m7tf1p01JqwiJ3Lb5NaegUFpvnuh/2sy65WuWPmtCMsQrKF9JfFo5iCAX8MfgFGWnjuVsypcmkEeN2+jfLmb58crZdF0QUuyPfkVMtGuiNIc/hquNn1fWvGRJP35EWeKN9ZtoMoHnY0KmHgKlmzB7ESQr+5jtdSHRodk20w3teONDrbthUFIISJOBrx9tHTNhItrQ2rfUJntqmHRW85bohBSYNXS7TFjHgM4QgqtzIgttKSQmo/THvWz9lW3bxpCZbzndoRADx7uO5NSm1ITAbYJaSs6SDnY0t3E/3x2Z5E1VVYdjV1uCFGXAJJnWUPoU="
  val csrfToken = "ed2e92ac3bd7a0d75f1fbbf3ae205471d0a3336e-1765280335361-e6e39b09186c841605270a62"
  val baseUrl = "https://video.code.dev-gutools.co.uk"

  val http = new Http("Cookie" -> cookie, "Csrf-Token" -> csrfToken)
  val api = new AtomMakerApi(http, baseUrl)
  val dimensionsHelper = new DimensionsHelper(http)

  val atomIds = api.getAtomIdsForPlatform("url")

  // create action plan to update atoms
  val plan = atomIds.flatMap { atomId =>
    api.getMediaAtom(atomId).flatMap { atom =>
      val updatedAssets = atom.assets.map {
        case asset if asset.mimeType.contains("text/vtt") && asset.assetType != Subtitles =>
          println(s"wrong asset type for VTT $atomId: $asset -> Subtitles")
          asset.copy(assetType = Subtitles)
        case asset if asset.assetType == Video && asset.platform == Url && asset.dimensions.isEmpty =>
          val dims = dimensionsHelper.readDimensions(asset)
          println(s"missing dimensions $atomId: $asset -> $dims")
          asset.copy(dimensions = dims)
        case asset => asset
      }
      if (updatedAssets != atom.assets)
        Some(UpdateAction(atom, atom.copy(assets = updatedAssets)))
      else
        None
    }
  }

  println("-----------")

  plan.foreach { action =>
    println(action.atom)
    println(action.updatedAtom)
    println(s"created ${action.atom.contentChangeDetails.created}")
    println(s"modified ${action.atom.contentChangeDetails.lastModified}")
    println(s"published ${action.atom.contentChangeDetails.published}")

    if (action.atom.contentChangeDetails.published.isDefined) {
      api.getPublishedMediaAtom(action.atom.id).foreach { publishedAtom =>
        println(s"published atom is up-to-date? ${publishedAtom == action.atom}, ${publishedAtom.contentChangeDetails.revision == action.atom.contentChangeDetails.revision}")
      }
    }

    println

    // TODO: This update is working for non-published atoms, but what should happen when published - do we need to publish again?
    //api.updateMediaAtom(action.updatedAtom)
  }
  // apply plan and verify



}

