package controllers

import software.amazon.awssdk.services.sfn.model.{
  ExecutionListItem,
  ExecutionStatus
}
import com.gu.atom.data.{DataStoreResultUtil, PreviewDynamoDataStoreV2}
import com.gu.media.{MediaAtomMakerPermissionsProvider, TestHelpers}
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
  val mockPreviewDataStore = mock[PreviewDynamoDataStoreV2]
  val mockScanamo = mock[Scanamo]

  when(mockDataStores.preview).thenReturn(mockPreviewDataStore)
  when(mackAwsConfig.scanamo).thenReturn(mockScanamo)
  when(
    mockScanamo.exec(any[ScanamoOps[Option[Either[DynamoReadError, Upload]]]]())
  ).thenReturn(Some(Left(MissingProperty)))
  when(mockStepFunctions.getEventsInReverseOrder(any())).thenReturn(Nil)

  val uploadController = new UploadController(
    mockAuthActions,
    mackAwsConfig,
    mockStepFunctions,
    mockDataStores,
    mockPermissions,
    mockYouTube,
    stubControllerComponents
  )

  "list" should "prepare a list of ClientAssets from the existing atom when no jobs are running or failed" in {
    // existing video versions 1.13 and 2.1
    mockAtom(
      atomAssets(2, 1) ++ atomAssets(1, 13),
      lastModified = "2025-09-03T12:52:22Z"
    )
    mockUploads(
      List(
        upload(1, 13, "2025-08-21T11:25:26Z"),
        upload(2, 1, "2025-09-03T12:49:51Z")
      )
    )
    mockJobs(Nil)

    // expect equivalent client assets with no processing info
    val expected = List(
      ClientAsset(
        "2",
        Some(selfHostedAsset(2, 1)),
        processing = None,
        Some(clientAssetMetadata(2, 1, "2025-09-03T12:49:51Z"))
      ),
      ClientAsset(
        "1",
        Some(selfHostedAsset(1, 13)),
        processing = None,
        Some(clientAssetMetadata(1, 13, "2025-08-21T11:25:26Z"))
      )
    )

    uploadController.clientAssetsForAtom(
      "ace3fcf6-1378-41db-9d21-f3fc07072ab2"
    ) shouldBe expected
  }

  it should "derive a new asset from the running job when a new video upload is in progress" in {
    // existing video version 1.13
    mockAtom(atomAssets(1, 13), lastModified = "2025-09-03T12:52:22Z")
    mockUploads(List(upload(1, 13, "2025-08-21T11:25:26Z")))

    // state machine running to upload video v2
    val job2running = videoUploadJob(
      2,
      ExecutionStatus.RUNNING,
      started = "2025-09-03T12:59:51Z"
    )
    mockJobs(List(job2running))
    when(mockStepFunctions.getTaskEntered(any())).thenReturn(
      Some(
        "AddInitialUploadDataToCache" -> upload(2, 0, "2025-09-03T12:49:51Z")
      )
    )
    when(mockStepFunctions.getExecutionFailed(any())).thenReturn(None)

    // expect client asset for v2 with processing data and no asset info
    val expectedProcessing = ClientAssetProcessing(
      "AddInitialUploadDataToCache",
      failed = false,
      None,
      None
    )
    val expectedMetadata = clientAssetMetadata(2, 0, "2025-09-03T12:59:51Z")
    val expected = List(
      ClientAsset(
        "2",
        asset = None,
        Some(expectedProcessing),
        Some(expectedMetadata)
      ),
      ClientAsset(
        "1",
        Some(selfHostedAsset(1, 13)),
        processing = None,
        Some(clientAssetMetadata(1, 13, "2025-08-21T11:25:26Z"))
      )
    )

    uploadController.clientAssetsForAtom(
      "ace3fcf6-1378-41db-9d21-f3fc07072ab2"
    ) shouldBe expected
  }

  it should "update an existing asset with progress of the running job when a subtitle is being uploaded" in {
    // existing video versions 1.13 and 2.1
    mockAtom(
      atomAssets(2, 1) ++ atomAssets(1, 13),
      lastModified = "2025-09-03T12:52:22Z"
    )
    mockUploads(
      List(
        upload(1, 13, "2025-08-21T11:25:26Z"),
        upload(2, 1, "2025-09-03T12:49:51Z")
      )
    )

    // state machine job is running to update subtitles on v2
    val job2running = subtitleUploadJob(
      2,
      2,
      ExecutionStatus.RUNNING,
      started = "2025-09-03T12:59:51Z"
    )
    mockJobs(List(job2running))
    when(mockStepFunctions.getTaskEntered(any())).thenReturn(
      Some(
        "AddInitialUploadDataToCache" -> upload(2, 2, "2025-09-03T12:59:51Z")
      )
    )
    when(mockStepFunctions.getExecutionFailed(any())).thenReturn(None)

    // expect client asset for v2 with asset and processing info
    val expectedProcessing = ClientAssetProcessing(
      "AddInitialUploadDataToCache",
      failed = false,
      None,
      None
    )
    val expectedMetadata = clientAssetMetadata(2, 1, "2025-09-03T12:59:51Z")
    val expected = List(
      ClientAsset(
        "2",
        Some(selfHostedAsset(2, 1)),
        Some(expectedProcessing),
        Some(expectedMetadata)
      ),
      ClientAsset(
        "1",
        Some(selfHostedAsset(1, 13)),
        processing = None,
        Some(clientAssetMetadata(1, 13, "2025-08-21T11:25:26Z"))
      )
    )

    uploadController.clientAssetsForAtom(
      "ace3fcf6-1378-41db-9d21-f3fc07072ab2"
    ) shouldBe expected
  }

  it should "update an existing asset with progress of the running job when processing fails" in {
    // existing video versions 1.13 and 2.1
    mockAtom(
      atomAssets(2, 1) ++ atomAssets(1, 13),
      lastModified = "2025-09-03T12:52:22Z"
    )
    mockUploads(
      List(
        upload(1, 13, "2025-08-21T11:25:26Z"),
        upload(2, 1, "2025-09-03T12:49:51Z")
      )
    )

    // state machine job to update subtitles has failed
    val job2failed = subtitleUploadJob(
      2,
      2,
      ExecutionStatus.FAILED,
      started = "2025-09-03T12:59:51Z"
    )
    mockJobs(List(job2failed))

    // todo
    when(mockStepFunctions.getTaskEntered(any())).thenReturn(
      Some("GetTranscodingProgressV2" -> upload(2, 2, "2025-09-03T12:59:51Z"))
    )
    when(mockStepFunctions.getExecutionFailed(any()))
      .thenReturn(Some("Job failed"))

    // expect client asset for v2 with failed processing state
    val expectedProcessing =
      ClientAssetProcessing("Job failed", failed = true, None, None)
    val expectedMetadata = clientAssetMetadata(2, 1, "2025-09-03T12:59:51Z")
    val expected = List(
      ClientAsset(
        "2",
        Some(selfHostedAsset(2, 1)),
        Some(expectedProcessing),
        Some(expectedMetadata)
      ),
      ClientAsset(
        "1",
        Some(selfHostedAsset(1, 13)),
        None,
        Some(clientAssetMetadata(1, 13, "2025-08-21T11:25:26Z"))
      )
    )

    uploadController.clientAssetsForAtom(
      "ace3fcf6-1378-41db-9d21-f3fc07072ab2"
    ) shouldBe expected
  }

  it should "return the existing assets when a successful upload has followed a failed upload" in {
    // asset has been successfully updated to v2.3
    mockAtom(
      atomAssets(2, 3) ++ atomAssets(1, 13),
      lastModified = "2025-09-03T13:05:22Z"
    )
    mockUploads(
      List(
        upload(1, 13, "2025-08-21T11:25:26Z"),
        upload(2, 3, "2025-09-03T13:02:00Z")
      )
    )

    // but failed job for v2.2 is still hanging around
    val job2failed = subtitleUploadJob(
      2,
      2,
      ExecutionStatus.FAILED,
      started = "2025-09-03T12:59:51Z"
    )
    mockJobs(List(job2failed))
    when(mockStepFunctions.getTaskEntered(any())).thenReturn(
      Some("GetTranscodingProgressV2" -> upload(2, 2, "2025-09-03T12:59:51Z"))
    )
    when(mockStepFunctions.getExecutionFailed(any()))
      .thenReturn(Some("Job failed"))

    // result should ignore the failed job
    val expected = List(
      ClientAsset(
        "2",
        Some(selfHostedAsset(2, 3)),
        None,
        Some(clientAssetMetadata(2, 3, "2025-09-03T13:02:00Z"))
      ),
      ClientAsset(
        "1",
        Some(selfHostedAsset(1, 13)),
        None,
        Some(clientAssetMetadata(1, 13, "2025-08-21T11:25:26Z"))
      )
    )

    uploadController.clientAssetsForAtom(
      "ace3fcf6-1378-41db-9d21-f3fc07072ab2"
    ) shouldBe expected
  }

  private def millis(isoDateTime: String): Long =
    DateTime.parse(isoDateTime).getMillis

  private def mockAtom(assets: List[Asset], lastModified: String): Unit = {
    val atom = TestHelpers.emptyAppMediaAtom.copy(
      id = "ace3fcf6-1378-41db-9d21-f3fc07072ab2",
      contentChangeDetails = ContentChangeDetails(
        Some(
          ChangeRecord(
            DateTime.parse(lastModified),
            Some(User("jo.blogs@guardian.co.uk", Some("Jo"), Some("Blogs")))
          )
        ),
        Some(
          ChangeRecord(
            DateTime.parse("2025-08-21T11:24:20Z"),
            Some(User("jo.blogs@guardian.co.uk", Some("Jo"), Some("Blogs")))
          )
        ),
        None,
        16,
        None,
        None,
        None
      ),
      assets = assets,
      activeVersion = Some(1),
      title = "Loop: Japan fireball",
      youtubeTitle = "Loop: Japan fireball",
      blockAds = false
    )
    when(mockPreviewDataStore.getAtom("ace3fcf6-1378-41db-9d21-f3fc07072ab2"))
      .thenReturn(DataStoreResultUtil.succeed(atom.asThrift))
  }

  private def mockUploads(uploads: List[Upload]): Unit =
    uploads.foreach { upload =>
      when(mockStepFunctions.getById(upload.id)).thenReturn(Some(upload))
    }

  private def mockJobs(jobs: List[ExecutionListItem]): Unit = {
    when(mockStepFunctions.getJobs("ace3fcf6-1378-41db-9d21-f3fc07072ab2"))
      .thenReturn(jobs)
  }

  private def atomAssets(assetVersion: Int, subtitleVersion: Int) = List(
    Asset(
      AssetType.Video,
      assetVersion,
      s"https://uploads.guimcode.co.uk/2025/09/03/Loop__Japan_fireball--ace3fcf6-1378-41db-9d21-f3fc07072ab2-$assetVersion.0.mp4",
      Platform.Url,
      Some("video/mp4"),
      Some(ImageAssetDimensions(1280, 720)),
      Some("16:9")
    ),
    Asset(
      AssetType.Video,
      assetVersion,
      s"https://uploads.guimcode.co.uk/2025/09/03/Loop__Japan_fireball--ace3fcf6-1378-41db-9d21-f3fc07072ab2-$assetVersion.$subtitleVersion.m3u8",
      Platform.Url,
      Some("application/vnd.apple.mpegurl"),
      Some(ImageAssetDimensions(1280, 720)),
      Some("16:9")
    )
  )

  private def videoUploadJob(
      assetVersion: Int,
      status: ExecutionStatus,
      started: String
  ) = ExecutionListItem
    .builder()
    .executionArn(
      s"arn:aws:states:eu-west-1:xxxxx:execution:VideoPipelineDEV-PGZ5E0CNI0QG:ace3fcf6-1378-41db-9d21-f3fc07072ab2-$assetVersion"
    )
    .stateMachineArn(
      "arn:aws:states:eu-west-1:xxxxx:stateMachine:VideoPipelineDEV-PGZ5E0CNI0QG"
    )
    .name(s"ace3fcf6-1378-41db-9d21-f3fc07072ab2-$assetVersion")
    .status(status)
    .startDate(Instant.parse(started))
    .build()

  private def subtitleUploadJob(
      assetVersion: Int,
      subtitleVersion: Int,
      status: ExecutionStatus,
      started: String
  ) = ExecutionListItem
    .builder()
    .executionArn(
      s"arn:aws:states:eu-west-1:xxxxx:execution:VideoPipelineDEV-PGZ5E0CNI0QG:ace3fcf6-1378-41db-9d21-f3fc07072ab2-$assetVersion.$subtitleVersion"
    )
    .stateMachineArn(
      "arn:aws:states:eu-west-1:xxxxx:stateMachine:VideoPipelineDEV-PGZ5E0CNI0QG"
    )
    .name(
      s"ace3fcf6-1378-41db-9d21-f3fc07072ab2-$assetVersion.$subtitleVersion"
    )
    .status(status)
    .startDate(Instant.parse(started))
    .build()

  private def selfHostedAsset(assetVersion: Int, subtitleVersion: Int) =
    SelfHostedAsset(
      List(
        VideoSource(
          s"https://uploads.guimcode.co.uk/2025/09/03/Loop__Japan_fireball--ace3fcf6-1378-41db-9d21-f3fc07072ab2-$assetVersion.0.mp4",
          "video/mp4",
          Some(1280),
          Some(720)
        ),
        VideoSource(
          s"https://uploads.guimcode.co.uk/2025/09/03/Loop__Japan_fireball--ace3fcf6-1378-41db-9d21-f3fc07072ab2-$assetVersion.$subtitleVersion.m3u8",
          "application/vnd.apple.mpegurl",
          Some(1280),
          Some(720)
        )
      )
    )

  private val videoFiles =
    List("Japan fireball.mp4", "file_example_MP4_480_1_5MG.mp4")
  private val subtitleFiles = List("different.srt", "different (1).srt")

  private def upload(
      videoVersion: Int,
      subtitleVersion: Int,
      started: String
  ) = {
    Upload(
      s"ace3fcf6-1378-41db-9d21-f3fc07072ab2-$videoVersion",
      Nil,
      UploadMetadata(
        "jo.blogs@guardian.co.uk",
        "bucket",
        "region",
        "title",
        null,
        iconikData = None,
        null,
        originalFilename = Some(videoFiles(videoVersion - 1)),
        subtitleSource =
          if (subtitleVersion > 0)
            Some(
              VideoSource(
                subtitleFiles(videoVersion - 1),
                "application/x-subrip"
              )
            )
          else
            None,
        subtitleVersion =
          if (subtitleVersion > 0) Some(subtitleVersion) else None,
        startTimestamp = Some(millis(started)),
        selfHost = true
      ),
      UploadProgress(1, 0, true, true, 0)
    )
  }

  private def clientAssetMetadata(
      videoVersion: Int,
      subtitleVersion: Int,
      started: String
  ) = ClientAssetMetadata(
    originalFilename = Some(videoFiles(videoVersion - 1)),
    subtitleFilename =
      if (subtitleVersion > 0) Some(subtitleFiles(videoVersion - 1)) else None,
    startTimestamp = Some(millis(started)),
    user = "jo.blogs@guardian.co.uk"
  )
}
