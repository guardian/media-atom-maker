package controllers

import com.amazonaws.services.stepfunctions.model.{ExecutionListItem, ExecutionStatus}
import com.gu.atom.data.{DataStoreResultUtil, PreviewDynamoDataStore}
import com.gu.media.MediaAtomMakerPermissionsProvider
import com.gu.media.model._
import com.gu.media.upload.model.{Upload, UploadMetadata, UploadProgress}
import com.gu.media.youtube.YouTubeVideos
import com.gu.pandahmac.HMACAuthActions
import data.DataStores
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, when}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.test.Helpers
import util.{AWSConfig, StepFunctions}
import org.scanamo.ops.ScanamoOps
import org.scanamo.{DynamoReadError, MissingProperty, Scanamo}

import java.time.Instant

class UploadControllerTest extends AnyFlatSpec with Matchers {

  val mockAuthActions = mock[HMACAuthActions]
  val mackAwsConfig = mock[AWSConfig]
  val mockStepFunctions = mock[StepFunctions]
  val mockDataStores = mock[DataStores]
  val mockPermissions = mock[MediaAtomMakerPermissionsProvider]
  val mockYouTube = mock[YouTubeVideos]
  val stubControllerComponents = Helpers.stubControllerComponents()
  val mockPreviewDataStore = mock[PreviewDynamoDataStore]
  val mockScanamo = mock[Scanamo]

  when(mockDataStores.preview).thenReturn(mockPreviewDataStore)
  when(mackAwsConfig.scanamo).thenReturn(mockScanamo)
  when(mockScanamo.exec(any[ScanamoOps[Option[Either[DynamoReadError, Upload]]]]())).thenReturn(Some(Left(MissingProperty)))
  when(mockStepFunctions.getEventsInReverseOrder(any())).thenReturn(Nil)

  val uploadController = new UploadController(
    mockAuthActions, mackAwsConfig, mockStepFunctions, mockDataStores, mockPermissions, mockYouTube, stubControllerComponents)

  val upload1 = Upload("ace3fcf6-1378-41db-9d21-f3fc07072ab2-1", Nil,
    UploadMetadata(
      "jo.blogs@guardian.co.uk", "bucket", "region", "title", null, null,
      originalFilename = Some("Japan fireball.mp4"),
      subtitleSource = Some(VideoSource("different.srt", "application/x-subrip")),
      startTimestamp = Some(1755775526136L),
      selfHost = true
    ),
    UploadProgress(1, 0, true, true, 0)
  )
  val upload2 = Upload("ace3fcf6-1378-41db-9d21-f3fc07072ab2-2", Nil,
    UploadMetadata(
      "jo.blogs@guardian.co.uk", "bucket", "region", "title", null, null,
      originalFilename = Some("file_example_MP4_480_1_5MG.mp4"),
      subtitleSource = Some(VideoSource("different (1).srt", "application/x-subrip")),
      startTimestamp = Some(1756903791627L),
      selfHost = true
    ),
    UploadProgress(1, 0, true, true, 0)
  )

  val clientAssetMetadata1 = ClientAssetMetadata(
    Some("Japan fireball.mp4"),
    Some("different.srt"),
    Some(1755775526136L),
    "jo.blogs@guardian.co.uk"
  )
  val clientAssetMetadata2 = ClientAssetMetadata(
    Some("file_example_MP4_480_1_5MG.mp4"),
    Some("different (1).srt"),
    Some(1756903791627L),
    "jo.blogs@guardian.co.uk"
  )

  "list" should "prepare a list of ClientAssets from the existing atom when no jobs are running or failed" in {
    mockAtom(atomAssets(2, 1) ++ atomAssets(1, 13))
    mockUploads(List(upload1, upload2))
    mockJobs(Nil)

    val expected = List(
      ClientAsset("2", Some(selfHostedAsset(2, 1)), processing = None, Some(clientAssetMetadata2)),
      ClientAsset("1", Some(selfHostedAsset(1, 13)), processing = None, Some(clientAssetMetadata1))
    )

    uploadController.clientAssetsForAtom("ace3fcf6-1378-41db-9d21-f3fc07072ab2") shouldBe expected
  }

  it should "derive a new asset from the running job when a new video upload is in progress" in {
    mockAtom(atomAssets(1, 13))
    mockUploads(List(upload1))

    val jobStartTime = "2025-09-03T12:49:51Z"
    val job2running = videoUploadJob(2, ExecutionStatus.RUNNING, jobStartTime)
    mockJobs(List(job2running))
    when(mockStepFunctions.getTaskEntered(any())).thenReturn(Some("AddInitialUploadDataToCache" -> upload2))
    when(mockStepFunctions.getExecutionFailed(any())).thenReturn(None)

    val expectedProcessing = ClientAssetProcessing("AddInitialUploadDataToCache", failed = false, None, None)
    val expectedMetadata = clientAssetMetadata2.copy(startTimestamp = Some(millis(jobStartTime)))
    val expected = List(
      ClientAsset("2", asset = None, Some(expectedProcessing), Some(expectedMetadata)),
      ClientAsset("1", Some(selfHostedAsset(1, 13)), processing = None, Some(clientAssetMetadata1))
    )

    uploadController.clientAssetsForAtom("ace3fcf6-1378-41db-9d21-f3fc07072ab2") shouldBe expected
  }

  it should "update an existing asset with progress of the running job when a subtitle is being uploaded" in {
    mockAtom(atomAssets(2, 1) ++ atomAssets(1, 13))
    mockUploads(List(upload1, upload2))

    val jobStartTime = "2025-09-03T12:59:51Z"
    val job2running = subtitleUploadJob(2, 2, ExecutionStatus.RUNNING, jobStartTime)
    mockJobs(List(job2running))
    when(mockStepFunctions.getTaskEntered(any())).thenReturn(Some("AddInitialUploadDataToCache" -> upload2))
    when(mockStepFunctions.getExecutionFailed(any())).thenReturn(None)

    val expectedProcessing = ClientAssetProcessing("AddInitialUploadDataToCache", failed = false, None, None)
    val expectedMetadata = clientAssetMetadata2.copy(startTimestamp = Some(millis(jobStartTime)))
    val expected = List(
      ClientAsset("2", Some(selfHostedAsset(2, 1)), Some(expectedProcessing), Some(expectedMetadata)),
      ClientAsset("1", Some(selfHostedAsset(1, 13)), None, Some(clientAssetMetadata1))
    )

    uploadController.clientAssetsForAtom("ace3fcf6-1378-41db-9d21-f3fc07072ab2") shouldBe expected
  }

  it should "update an existing asset with progress of the running job when processing fails" in {
    mockAtom(atomAssets(2, 1) ++ atomAssets(1, 13))
    mockUploads(List(upload1, upload2))

    val jobStartTime = "2025-09-03T13:09:51Z"
    val job2failed = subtitleUploadJob(2, 2, ExecutionStatus.FAILED, jobStartTime)
    mockJobs(List(job2failed))
    when(mockStepFunctions.getTaskEntered(any())).thenReturn(Some("GetTranscodingProgressV2" -> upload2))
    when(mockStepFunctions.getExecutionFailed(any())).thenReturn(Some("Job failed"))

    val expectedProcessing = ClientAssetProcessing("Job failed", failed = true, None, None)
    val expectedMetadata = clientAssetMetadata2.copy(startTimestamp = Some(millis(jobStartTime)))
    val expected = List(
      ClientAsset("2", Some(selfHostedAsset(2, 1)), Some(expectedProcessing), Some(expectedMetadata)),
      ClientAsset("1", Some(selfHostedAsset(1, 13)), None, Some(clientAssetMetadata1))
    )

    uploadController.clientAssetsForAtom("ace3fcf6-1378-41db-9d21-f3fc07072ab2") shouldBe expected
  }

  it should "return the existing assets when a successful upload has followed a failed upload" in {
    mockAtom(atomAssets(2, 3) ++ atomAssets(1, 13))
    mockUploads(List(upload1, upload2))

    val job2failed = subtitleUploadJob(2, 2, ExecutionStatus.FAILED, "2025-09-03T13:09:51Z")
    val job2succeeded = subtitleUploadJob(2, 3, ExecutionStatus.SUCCEEDED, "2025-09-03T13:19:51Z")
    mockJobs(List(job2failed, job2succeeded))
    when(mockStepFunctions.getTaskEntered(any())).thenReturn(Some("GetTranscodingProgressV2" -> upload2))
    when(mockStepFunctions.getExecutionFailed(any())).thenReturn(Some("Job failed"))

    val expected = List(
      ClientAsset("2", Some(selfHostedAsset(2, 3)), None, Some(clientAssetMetadata2)),
      ClientAsset("1", Some(selfHostedAsset(1, 13)), None, Some(clientAssetMetadata1))
    )

    uploadController.clientAssetsForAtom("ace3fcf6-1378-41db-9d21-f3fc07072ab2") shouldBe expected
  }

  private def millis(isoDateTime: String): Long = DateTime.parse(isoDateTime).getMillis

  private def mockAtom(assets: List[Asset]): Unit = {
    val atom = MediaAtom("ace3fcf6-1378-41db-9d21-f3fc07072ab2",
      List(),
      ContentChangeDetails(Some(
        ChangeRecord(
          DateTime.parse("2025-09-03T13:52:22.526+01:00"),
          Some(User("jo.blogs@guardian.co.uk",Some("Jo"),Some("Blogs")))
        )),
        Some(ChangeRecord(
          DateTime.parse("2025-08-21T12:24:20.411+01:00"),
          Some(User("jo.blogs@guardian.co.uk",Some("Jo"),Some("Blogs")))
        )),
        None,16,None,None,None
      ),
      assets,
      Some(1),
      "Loop: Japan fireball",
      Category.News,
      None,None,None,None,None,None,None,None,List(),List(),List(),List(),None,None,None,None,None,None,None,
      "Loop: Japan fireball",
      None,false,None,None,None
    )
    when(mockPreviewDataStore.getAtom("ace3fcf6-1378-41db-9d21-f3fc07072ab2")).thenReturn(DataStoreResultUtil.succeed(atom.asThrift))
  }

  private def mockUploads(uploads: List[Upload]): Unit =
    uploads.foreach { upload =>
      when(mockStepFunctions.getById(upload.id)).thenReturn(Some(upload))
    }

  private def mockJobs(jobs: List[ExecutionListItem]): Unit = {
    when(mockStepFunctions.getJobs("ace3fcf6-1378-41db-9d21-f3fc07072ab2")).thenReturn(jobs)
  }

  private def atomAssets(assetVersion: Int, subtitleVersion: Int) = List(
    Asset(AssetType.Video, assetVersion,
      s"https://uploads.guimcode.co.uk/2025/09/03/Loop__Japan_fireball--ace3fcf6-1378-41db-9d21-f3fc07072ab2-$assetVersion.0.mp4",
      Platform.Url, Some("video/mp4")),
    Asset(AssetType.Video, assetVersion,
      s"https://uploads.guimcode.co.uk/2025/09/03/Loop__Japan_fireball--ace3fcf6-1378-41db-9d21-f3fc07072ab2-$assetVersion.$subtitleVersion.m3u8",
      Platform.Url, Some("application/vnd.apple.mpegurl"))
  )

  private def videoUploadJob(assetVersion: Int, status: ExecutionStatus, isoStartDateTime: String) = new ExecutionListItem()
    .withExecutionArn(s"arn:aws:states:eu-west-1:xxxxx:execution:VideoPipelineDEV-PGZ5E0CNI0QG:ace3fcf6-1378-41db-9d21-f3fc07072ab2-$assetVersion")
    .withStateMachineArn("arn:aws:states:eu-west-1:xxxxx:stateMachine:VideoPipelineDEV-PGZ5E0CNI0QG")
    .withName(s"ace3fcf6-1378-41db-9d21-f3fc07072ab2-$assetVersion")
    .withStatus(status)
    .withStartDate(java.util.Date.from(Instant.parse(isoStartDateTime)))

  private def subtitleUploadJob(assetVersion: Int, subtitleVersion: Int, status: ExecutionStatus, isoStartDateTime: String) = new ExecutionListItem()
    .withExecutionArn(s"arn:aws:states:eu-west-1:xxxxx:execution:VideoPipelineDEV-PGZ5E0CNI0QG:ace3fcf6-1378-41db-9d21-f3fc07072ab2-$assetVersion.$subtitleVersion")
    .withStateMachineArn("arn:aws:states:eu-west-1:xxxxx:stateMachine:VideoPipelineDEV-PGZ5E0CNI0QG")
    .withName(s"ace3fcf6-1378-41db-9d21-f3fc07072ab2-$assetVersion.$subtitleVersion")
    .withStatus(status)
    .withStartDate(java.util.Date.from(Instant.parse(isoStartDateTime)))

  private def selfHostedAsset(assetVersion: Int, subtitleVersion: Int) = SelfHostedAsset(List(
    VideoSource(s"https://uploads.guimcode.co.uk/2025/09/03/Loop__Japan_fireball--ace3fcf6-1378-41db-9d21-f3fc07072ab2-$assetVersion.0.mp4","video/mp4"),
    VideoSource(s"https://uploads.guimcode.co.uk/2025/09/03/Loop__Japan_fireball--ace3fcf6-1378-41db-9d21-f3fc07072ab2-$assetVersion.$subtitleVersion.m3u8","application/vnd.apple.mpegurl")
  ))


}
