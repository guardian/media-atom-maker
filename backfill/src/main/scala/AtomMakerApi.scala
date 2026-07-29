import com.gu.media.model.MediaAtom
import play.api.libs.json.{JsArray, Json}

import scala.annotation.tailrec

class AtomMakerApi(http: Http, baseUrl: String) {

  def getAtomIds(limit: Int = 1000): List[String] =
    for {
      content <- http.get(s"$baseUrl/api/atoms?limit=$limit").toList
      atom <- (Json.parse(content) \ "atoms").as[JsArray].value
    } yield (atom \ "id").as[String]

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

 def updateMediaAtom(atom: MediaAtom): String = {
   val content = Json.toJson(atom).toString()
   @tailrec
   def attemptUpdate(attempt: Int): String = {
     try {
       http.put(s"$baseUrl/api/atoms/${atom.id}", content)
     } catch {
       case e: Exception if attempt < 5 =>
         val delay = (math.pow(2, attempt - 1).toLong) * 1000L
         println(s"Update failed for ${atom.id} (attempt $attempt): ${e.getMessage}. Retrying in $delay ms")
         Thread.sleep(delay)
         attemptUpdate(attempt + 1)
       case e: Exception =>
         println(s"Update failed for ${atom.id} after $attempt attempt(s): ${e.getMessage}")
         ""
     }
   }
   attemptUpdate(1)
 }

  def publishMediaAtom(atomId: String): String = {
    @tailrec
    def attemptUpdate(attempt: Int): String = {
      try {
        http.put(s"$baseUrl/api/atom/$atomId/publish", "")
      } catch {
        case e: Exception if attempt < 5 =>
          val delay = (math.pow(2, attempt - 1).toLong) * 1000L
          println(s"Publish failed for ${atomId} (attempt $attempt): ${e.getMessage}. Retrying in $delay ms")
          Thread.sleep(delay)
          attemptUpdate(attempt + 1)
        case e: Exception =>
          println(s"Publish failed for ${atomId} after $attempt attempt(s): ${e.getMessage}")
          ""
      }
    }
    attemptUpdate(1)
  }

}
