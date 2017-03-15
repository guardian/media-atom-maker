package util

import com.amazonaws.auth.InstanceProfileCredentialsProvider
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.model.{DescribeTagsRequest, Filter}
import com.amazonaws.util.EC2MetadataUtils
import com.gu.media.Settings
import com.gu.media.aws._
import com.gu.media.logging.KinesisLogging
import com.typesafe.config.Config

import scala.collection.JavaConverters._

class AWSConfig(override val config: Config)
  extends Settings
    with AwsAccess
    with CrossAccountAccess
    with S3Access
    with DynamoAccess
    with UploadAccess
    with KinesisAccess
    with ElasticTranscodeAccess
    with KinesisLogging {

  lazy val ec2Client = region.createClient(
    classOf[AmazonEC2Client],
    credsProvider,
    null
  )

  lazy val composerUrl = getMandatoryString("flexible.url")
  lazy val viewerUrl = getMandatoryString("viewer.url")

  lazy val gridUrl = getMandatoryString("grid.url")

  lazy val expiryPollerName = "Expiry"
  lazy val expiryPollerLastName = "Poller"

  final override def regionName = getString("aws.region")
  final override def instanceCredentials = InstanceProfileCredentialsProvider.getInstance()
  final override def localDevCredentials = getString("aws.profile").map(new ProfileCredentialsProvider(_))

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
