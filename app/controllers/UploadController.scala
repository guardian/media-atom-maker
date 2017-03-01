package controllers

import java.util.UUID

import com.gu.media.logging.Logging
import com.gu.media.upload._
import com.gu.pandahmac.HMACAuthActions
import play.api.libs.json.Json
import play.api.mvc.Controller
import util.AWSConfig

class UploadController(val authActions: HMACAuthActions, awsConfig: AWSConfig)
  extends Controller with Logging with JsonRequestParsing {

  import authActions.APIHMACAuthAction

  private val table = new DynamoUploadsTable(awsConfig)
  private val creds = new CredentialsGenerator(awsConfig)

  def list(atomId: String) = APIHMACAuthAction {
    val results = table.list(atomId)
    Ok(Json.toJson(results))
  }

  def create = APIHMACAuthAction { implicit raw =>
    parse(raw) { req: CreateAPIRequest =>
      log.info(s"Request for upload under atom ${req.atomId}. filename=${req.filename}. size=${req.size}")

      val id = UUID.randomUUID().toString

      val chunks = calculateChunks(req.size)
      val forTable = generateForTable(req.atomId, id, chunks)
      val forClient = generateForClient(id, chunks)

      table.put(forTable)

      Ok(Json.toJson(forClient))
    }
  }

  def stop(id: String) = APIHMACAuthAction {
    table.delete(id)
    NoContent
  }

  def credentials(id: String, part: String) = APIHMACAuthAction {
    try {
      val key = UploadPartKey(awsConfig.userUploadFolder, id, part.toInt)
      val credentials = creds.forPart(key)

      Ok(Json.toJson(credentials))
    } catch {
      case err: NumberFormatException =>
        BadRequest(err.getMessage)
    }
  }

  private def generateForTable(atomId: String, id: String, chunks: List[(Long, Long)]): UploadEntry = {
    val partsForTable = chunks.map { case(start, end) => UploadPartEntry(start, end) }
    UploadEntry(id, atomId, partsForTable)
  }

  private def generateForClient(id: String, chunks: List[(Long, Long)]): CreateAPIResponse = {
    val partsForClient = chunks.zipWithIndex.map { case((start, end), ix) =>
      APIPart(ix, UploadPartKey(awsConfig.userUploadFolder, id, ix).toString, start, end)
    }

    CreateAPIResponse(id, awsConfig.region.getName, awsConfig.userUploadBucket, partsForClient)
  }
}
