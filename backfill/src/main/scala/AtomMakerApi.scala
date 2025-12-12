import com.gu.media.model.MediaAtom
import play.api.libs.json.{JsArray, Json}

class AtomMakerApi(http: Http, baseUrl: String) {

  def getAtomIdsForPlatform(platform: String, limit: Int = 1000): List[String] = // 'url' or 'youtube'
    for {
      content <- http.get(s"$baseUrl/api/atoms?limit=$limit&mediaPlatform=$platform").toList
      atom <- (Json.parse(content) \ "atoms").as[JsArray].value
    } yield (atom \ "id").as[String]

  def getMediaAtom(id: String): Option[MediaAtom] = {
    http.get(s"$baseUrl/api/atoms/$id").map { atom =>
      Json.parse(atom).as[MediaAtom]
    }
  }

  def getPublishedMediaAtom(id: String): Option[MediaAtom] = {
    http.get(s"$baseUrl/api/atoms/$id/published").flatMap { atom =>
      Json.parse(atom).asOpt[MediaAtom].orElse {
        println(s"Couldn't parse media atom from $atom")
        None
      }
    }
  }

  def updateMediaAtom(atom: MediaAtom): Option[String] = {
    val content = Json.toJson(atom).toString()
    http.put(s"$baseUrl/api/atoms/${atom.id}", content)
  }

  def publishMediaAtom(atomId: String): Option[String] = {
    http.put(s"$baseUrl/api/atom/$atomId/publish", "")
  }

}
