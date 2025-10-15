package util

import com.amazonaws.services.ec2.AmazonEC2ClientBuilder
import com.amazonaws.services.ec2.model.{DescribeTagsRequest, Filter}
import com.amazonaws.util.EC2MetadataUtils
import com.gu.media.Settings
import com.gu.media.aws._
import com.gu.media.logging.{Logging}
import com.typesafe.config.Config

import scala.jdk.CollectionConverters._

class AWSConfig(
    override val config: Config,
    override val credentials: AwsCredentials
) extends Settings
    with Logging
    with AwsAccess
    with S3Access
    with DynamoAccess
    with UploadAccess
    with KinesisAccess
    with ElasticTranscodeAccess
    with SQSAccess
    with SNSAccess
    with SESSettings {

  lazy val ec2Client = AmazonEC2ClientBuilder
    .standard()
    .withRegion(region.getName)
    .withCredentials(credentials.instance.awsV1Creds)
    .build()

  lazy val pinboardLoaderUrl = getString("panda.domain").map(domain =>
    s"https://pinboard.$domain/pinboard.loader.js"
  )
  lazy val composerUrl = getMandatoryString("flexible.url")
  lazy val workflowUrl = getMandatoryString("workflow.url")
  lazy val viewerUrl = getMandatoryString("viewer.url")

  lazy val gridUrl = getMandatoryString("grid.url")

  lazy val gaPropertyId: Option[String] = getString("gaPropertyId")

  lazy val targetingUrl = getMandatoryString("targeting.url")

  lazy val tagManagerUrl = getMandatoryString("tagManager.url")

  lazy val expiryPollerName = "Expiry"
  lazy val expiryPollerLastName = "Poller"

  final override def region = AwsAccess.regionFrom(this)

  final override def readTag(tagName: String) = {
    val tagsResult = ec2Client.describeTags(
      new DescribeTagsRequest().withFilters(
        new Filter("resource-type").withValues("instance"),
        new Filter("resource-id").withValues(EC2MetadataUtils.getInstanceId),
        new Filter("key").withValues(tagName)
      )
    )

    tagsResult.getTags.asScala.find(_.getKey == tagName).map(_.getValue)
  }
}
