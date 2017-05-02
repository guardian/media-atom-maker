import java.io.{FilterInputStream, InputStream}
import java.time.Instant
import java.util.UUID

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import com.google.common.io.ByteStreams
import com.gu.media.util.TestFilters
import integration.IntegrationTestBase
import integration.services.Config
import org.scalatest.CancelAfterFailure
import org.scalatest.time.{Minutes, Seconds, Span}
import play.api.libs.json.{JsArray, JsValue, Json}

class VideoUploadTests extends IntegrationTestBase with CancelAfterFailure {
  // TODO MRB: use STS credentials
  val credentials = new ProfileCredentialsProvider("media-service")
  val s3 = Region.getRegion(Regions.EU_WEST_1).createClient(classOf[AmazonS3Client], credentials, null)

  val sourceVideoBucket = "atom-maker-test-videos"
  val sourceVideo = "170502radcliffe.mp4"

  var atomId: String = ""
  var apiEndpoint: String = ""

  var uploadId: String = ""
  var uploadBucket: String = ""
  var uploadParts = List.empty[(String, Long, Long)]
  var uploadUri: Option[String] = None

  test(s"$targetBaseUrl is up") {
    val response = gutoolsGet(targetBaseUrl)
    response.code() should be (200)
    response.body().string() should include ("video")
  }

  test("Create a new atom") {
    val json = generateJson(
      title = s"${TestFilters.testAtomBaseName}-${UUID.randomUUID().toString}",
      description = "test atom",
      category = "News",
      channelId = Config.channelId,
      youtubeCategoryId = Config.youtubeCategoryId,
      expiryDate = Instant.now.toEpochMilli + (100 * 60 * 60 * 24)
    )

    val response = gutoolsPost(s"$targetBaseUrl/api2/atoms", jsonBody(json))

    response.code() should be(201)

    atomId = (Json.parse(response.body().string()) \ "id").get.as[String]

    addAtomToStore(atomId)

    apiEndpoint = apiUri(atomId)

    eventually {
      gutoolsGet(apiEndpoint).code() should be(200)
    }

    Json.parse(gutoolsGet(s"$targetBaseUrl/api2/atoms/$atomId/published").body().string()).toString() should be("{}")
  }

  test("Create an upload") {
    val source = s3.getObject(sourceVideoBucket, sourceVideo)
    val json = generateUploadRequest(atomId, source.getObjectMetadata.getContentLength)

    val response = gutoolsPost(s"$targetBaseUrl/api2/uploads?atomId=$atomId", jsonBody(json))
    val responseJson = Json.parse(response.body().string())

    response.code() should be(200)

    uploadId = (responseJson \ "id").as[String]
    uploadBucket = (responseJson \ "metadata" \ "bucket").as[String]
    uploadParts = (responseJson \ "parts").as[JsArray].value.map(parseUploadPart).toList
  }

  test("Upload parts") {
    val source = s3.getObject(sourceVideoBucket, sourceVideo).getObjectContent

    uploadParts.foreach { case(uploadKey, start, end) =>
      val length = end - start
      val part = slice(source, length)

      val metadata = new ObjectMetadata()
      metadata.setContentLength(length)

      s3.putObject(uploadBucket, uploadKey, part, metadata)

      val headers = List(Some("X-Upload-Key" -> uploadKey), uploadUri.map("X-Upload-Uri" -> _)).flatten

      val response = gutoolsPost(s"$targetBaseUrl/api2/uploads/$uploadId/complete", emptyBody, headers.toMap)
      uploadUri = Some((Json.parse(response.body().string()) \ "uploadUri").as[String])
    }
  }

  test("Add asset to atom") {
    implicit val patienceConfig = PatienceConfig(
      timeout = Span(10, Minutes),
      interval = Span(10, Seconds)
    )

    eventually {
      val assets = (Json.parse(gutoolsGet(apiEndpoint).body().string()) \ "data" \ "assets").as[JsArray].value
      assets should not be empty

      val asset = assets.head

      (asset \ "platform").as[String] should be("Youtube")
      addYouTubeVideoToStore((asset \ "id").as[String])
    }
  }

  private def parseUploadPart(part: JsValue): (String, Long, Long) =
    ((part \ "key").as[String], (part \ "start").as[Long], (part \ "end").as[Long])

  private def slice(input: InputStream, size: Long): InputStream = {
    new FilterInputStream(ByteStreams.limit(input, size)) {
      override def close(): Unit = {
        // nah keep it open :)
      }
    }
  }
}
