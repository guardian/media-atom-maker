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
    http.get(s"$baseUrl/api/atoms/$id/published").map { atom =>
      println(s"raw $atom")
      Json.parse(atom).as[MediaAtom]
    }
  }

  def updateMediaAtom(atom: MediaAtom): Unit = {
    val content = Json.toJson(atom).toString()
    println(content)
    val result = http.put(s"$baseUrl/api/atoms/${atom.id}", content)
    println(result)
  }

}
