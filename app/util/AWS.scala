package util

import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.model.{DescribeTagsRequest, Filter}
import com.amazonaws.services.kinesis.AmazonKinesisClient
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoderClient
import com.amazonaws.util.EC2MetadataUtils
import com.gu.media.aws._
import com.gu.media.logging.KinesisLogging
import com.typesafe.config.Config

import scala.collection.JavaConverters._

class AWSConfig(override val config: Config)
  extends AwsAccess
    with CrossAccountAccess
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

  override val stack = readTag("Stack")
  override val app = readTag("App")
  override val stage = readTag("Stage").getOrElse("DEV")

  lazy val composerUrl = getMandatoryString("flexible.url")

  lazy val gridUrl = stage match {
    case "PROD" => "https://media.gutools.co.uk"
    case _ => "https://media.test.dev-gutools.co.uk"
  }

  lazy val expiryPollerName = "Expiry"
  lazy val expiryPollerLastName = "Poller"



  def readTag(tagName: String) = {
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
