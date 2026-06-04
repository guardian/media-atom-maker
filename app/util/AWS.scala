package util

import software.amazon.awssdk.services.ec2.Ec2Client
import software.amazon.awssdk.services.ec2.model.{DescribeTagsRequest, Filter}
import com.gu.media.Settings
import com.gu.media.aws._
import com.gu.media.logging.Logging
import com.typesafe.config.Config
import software.amazon.awssdk.regions.internal.util.EC2MetadataUtils

import scala.jdk.CollectionConverters._
import scala.util.Try

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
    with SQSAccess
    with SNSAccess
    with SESSettings
    with SecretsManagerAccess {

  lazy val ec2Client = Ec2Client
    .builder()
    .region(awsV2Region)
    .credentialsProvider(credentials.instance.awsV2Creds)
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
    val tagsResult = Try {
      ec2Client.describeTags(
        DescribeTagsRequest
          .builder()
          .filters(
            Filter.builder().name("resource-type").values("instance").build(),
            Filter
              .builder()
              .name("resource-id")
              .values(EC2MetadataUtils.getInstanceId)
              .build(),
            Filter.builder().name("key").values(tagName).build()
          )
          .build()
      )
    }

    tagsResult.toOption.flatMap(
      _.tags().asScala.find(_.key == tagName).map(_.value)
    )
  }
}
