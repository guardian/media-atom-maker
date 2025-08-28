package util

import com.amazonaws.regions.{Region, RegionUtils}
import com.gu.contentatom.thrift.atom.media.{Asset, AssetType, Category, Platform, MediaAtom => ThriftMediaAtom}
import com.gu.contentatom.thrift.{Atom, AtomData, AtomType, ContentChangeDetails}
import com.gu.media.Settings
import com.gu.media.aws.{AwsAccess, AwsCredentials, UploadAccess}
import com.gu.media.model.{MediaAtom, PlutoSyncMetadataMessage, SelfHostedAsset, VideoSource}
import com.gu.media.upload.TranscoderOutputKey
import com.gu.media.upload.model.{CopyProgress, SelfHostedUploadMetadata, UploadMetadata, UploadPart, UploadProgress, UploadRequest}
import com.typesafe.config.Config
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.mockito.MockitoSugar.{when, withObjectSpied}
import play.api.Configuration

class UploadBuilderTest extends AnyFlatSpec with Matchers {

  private val atomId = "61e7a4c3-cb36-492d-889c-163abdae68e4"
  private val regionName = "eu-west-1"
  private val configData = List("aws.upload.bucket" -> "upload-test-bucket",
    "aws.upload.folder" -> "uploads",
    "aws.upload.role" -> "arn:dummy-aws-role",
    "aws.profile" -> "test",
    "aws.upload.accessKey" -> "dummyKey",
    "aws.upload.secretKey" -> "dummySecret")

  "build" should "create an Upload record for a new self-hosted video upload" in {
    val selfHostVideoRequest = UploadRequest(atomId, "my-video.mp4", 12345L, selfHost=true)

    withObjectSpied[TranscoderOutputKey.type] {
      when(TranscoderOutputKey.currentDate) thenReturn "2025/08/20"

      val upload = UploadBuilder.build(MediaAtom.fromThrift(atom), "jo.blogs@guardian.co.uk", 2L, selfHostVideoRequest, aws)

      upload.id shouldBe "61e7a4c3-cb36-492d-889c-163abdae68e4-2"
      upload.parts shouldBe List(
        UploadPart(key = "uploads/61e7a4c3-cb36-492d-889c-163abdae68e4-2/parts/0", start = 0L, end = 12345L))

      upload.metadata.user shouldBe "jo.blogs@guardian.co.uk"
      upload.metadata.bucket shouldBe "upload-test-bucket"
      upload.metadata.region shouldBe "eu-west-1"
      upload.metadata.title shouldBe "Atom Title"
      upload.metadata.pluto shouldBe PlutoSyncMetadataMessage(
        `type` = "video-upload",
        projectId = None,
        s3Key = "uploads/61e7a4c3-cb36-492d-889c-163abdae68e4-2/complete",
        atomId = "61e7a4c3-cb36-492d-889c-163abdae68e4",
        title = "Atom Title",
        user = "jo.blogs@guardian.co.uk",
        posterImageUrl = None)
      upload.metadata.runtime shouldBe SelfHostedUploadMetadata(jobs = List())
      upload.metadata.version should contain (2L)
      upload.metadata.selfHost shouldBe true
      upload.metadata.asset shouldBe Some(SelfHostedAsset(sources = List(
        VideoSource(src = "2025/08/20/Atom_Title--61e7a4c3-cb36-492d-889c-163abdae68e4-2.0.mp4", mimeType = "video/mp4"),
        VideoSource(src = "2025/08/20/Atom_Title--61e7a4c3-cb36-492d-889c-163abdae68e4-2.0.m3u8", mimeType = "application/vnd.apple.mpegurl")
      )))
      upload.metadata.originalFilename should contain ("my-video.mp4")
      upload.metadata.startTimestamp should not be empty
      upload.metadata.subtitleSource shouldBe empty
      upload.metadata.subtitleVersion shouldBe empty

      upload.progress shouldBe UploadProgress(
        chunksInS3 = 0,
        chunksInYouTube = 0,
        fullyUploaded = false,
        fullyTranscoded = false,
        retries = 0,
        copyProgress = None)
    }
  }

  "buildForSubtitleChange" should "modify the existing Upload record to reflect a subtitle change" in {
    val selfHostVideoRequest = UploadRequest(atomId, "my-video.mp4", 12345L, selfHost=true)

    withObjectSpied[TranscoderOutputKey.type] {
      when(TranscoderOutputKey.currentDate) thenReturn "2025/08/20"

      val videoUpload = UploadBuilder.build(MediaAtom.fromThrift(atom), "jo.blogs@guardian.co.uk", 2L, selfHostVideoRequest, aws)

      // simulate a completed video upload by updating the progress record
      val completedVideoUpload = videoUpload.copy(progress = completedProgress)

      // add subtitles
      val subtitleSource = VideoSource("uploads/61e7a4c3-cb36-492d-889c-163abdae68e4-2/subtitle.srt", "application/x-subrip")
      val subtitleUpload = UploadBuilder.buildForSubtitleChange(completedVideoUpload, Some(subtitleSource))

      // we expect the modified upload record to have bumped the minor version on the m3u8 filename,
      // stored the subtitle source and version and set the progress to not fully transcoded
      val expectedAsset = SelfHostedAsset(sources = List(
        VideoSource(src = "2025/08/20/Atom_Title--61e7a4c3-cb36-492d-889c-163abdae68e4-2.0.mp4", mimeType = "video/mp4"),
        VideoSource(src = "2025/08/20/Atom_Title--61e7a4c3-cb36-492d-889c-163abdae68e4-2.1.m3u8", mimeType = "application/vnd.apple.mpegurl")
      ))
      val expectedMetadata = completedVideoUpload.metadata.copy(
        asset = Some(expectedAsset),
        subtitleSource = Some(subtitleSource),
        subtitleVersion = Some(1))
      val expectedProgress = completedVideoUpload.progress.copy(fullyTranscoded = false)
      val expected = completedVideoUpload.copy(metadata = expectedMetadata, progress = expectedProgress)

      subtitleUpload shouldBe expected
    }
  }

  "buildForSubtitleChange" should "modify the existing Upload record to reflect subtitle removal" in {
    val selfHostVideoRequest = UploadRequest(atomId, "my-video.mp4", 12345L, selfHost=true)

    withObjectSpied[TranscoderOutputKey.type] {
      when(TranscoderOutputKey.currentDate) thenReturn "2025/08/20"

      val videoUpload = UploadBuilder.build(MediaAtom.fromThrift(atom), "jo.blogs@guardian.co.uk", 2L, selfHostVideoRequest, aws)

      // simulate a completed video upload by updating the progress record
      val completedVideoUpload = videoUpload.copy(progress = completedProgress)

      // simulate adding subtitles
      val subtitleSource = VideoSource("uploads/61e7a4c3-cb36-492d-889c-163abdae68e4-2/subtitle.srt", "application/x-subrip")
      val subtitleUpload = UploadBuilder.buildForSubtitleChange(completedVideoUpload, Some(subtitleSource))

      // remove subtitles
      val subtitlesRemovedUpload = UploadBuilder.buildForSubtitleChange(subtitleUpload, newSubtitleSource = None)

      // we expect the modified upload record to have bumped the minor version on the m3u8 filename,
      // removed the subtitle source and set the progress to not fully transcoded
      val expectedAsset = SelfHostedAsset(sources = List(
        VideoSource(src = "2025/08/20/Atom_Title--61e7a4c3-cb36-492d-889c-163abdae68e4-2.0.mp4", mimeType = "video/mp4"),
        VideoSource(src = "2025/08/20/Atom_Title--61e7a4c3-cb36-492d-889c-163abdae68e4-2.2.m3u8", mimeType = "application/vnd.apple.mpegurl")
      ))
      val expectedMetadata = subtitlesRemovedUpload.metadata.copy(
        asset = Some(expectedAsset),
        subtitleSource = None,
        subtitleVersion = Some(2))
      val expectedProgress = subtitlesRemovedUpload.progress.copy(fullyTranscoded = false)
      val expected = subtitlesRemovedUpload.copy(metadata = expectedMetadata, progress = expectedProgress)

      subtitlesRemovedUpload shouldBe expected
    }
  }

  private def asset: Asset = Asset(
    assetType = AssetType.Video,
    version = 1,
    id = "test",
    platform = Platform.Youtube,
    mimeType = None
  )

  private def atom: Atom = Atom(
    id = atomId,
    atomType = AtomType.Media,
    labels = Seq.empty,
    defaultHtml = "",
    data = AtomData.Media(thriftMediaAtom),
    contentChangeDetails = ContentChangeDetails(revision = 1)
  )

  private def thriftMediaAtom = ThriftMediaAtom(
    assets = Seq(asset),
    activeVersion = Some(1),
    title = "Atom Title",
    category = Category.Feature
  )

  private def aws = new Settings with AwsAccess with UploadAccess {

    override def config: Config = Configuration(configData: _*).underlying

    override def readTag(tag: String): Option[String] = None

    override val credentials: AwsCredentials = AwsCredentials.dev(Settings(config))

    override def region: Region = RegionUtils.getRegion(regionName)
  }

  private def completedProgress = UploadProgress(
    chunksInS3 = 1,
    chunksInYouTube = 0,
    fullyUploaded = true,
    fullyTranscoded = true,
    retries = 0,
    copyProgress = Some(CopyProgress(copyId = "1234", fullyCopied = true, eTags = Nil))
  )
}
