package util

import com.amazonaws.services.ec2.AmazonEC2ClientBuilder
import com.amazonaws.services.ec2.model.{DescribeTagsRequest, Filter}
import com.amazonaws.util.EC2MetadataUtils
import com.gu.media.Settings
import com.gu.media.aws._
import com.gu.media.logging.{KinesisLogging, Logging}
import com.typesafe.config.Config

import scala.collection.JavaConverters._

class AWSConfig(override val config: Config, override val credentials: AwsCredentials)
  extends Settings
    with Logging
    with AwsAccess
    with S3Access
    with DynamoAccess
    with PipelineAccess
    with KinesisAccess
    with ElasticTranscodeAccess
    with KinesisLogging
    with SQSAccess
    with SESSettings {

  lazy val ec2Client = AmazonEC2ClientBuilder
    .standard()
    .withRegion(region.getName)
    .withCredentials(credentials.instance)
    .build()

  lazy val composerUrl = getMandatoryString("flexible.url")
  lazy val workflowUrl = getMandatoryString("workflow.url")
  lazy val viewerUrl = getMandatoryString("viewer.url")

  lazy val gridUrl = getMandatoryString("grid.url")

  lazy val expiryPollerName = "Expiry"
  lazy val expiryPollerLastName = "Poller"

  final override def regionName = getString("aws.region")

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
