package util

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{AWSCredentialsProviderChain, _}
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.model.{DescribeTagsRequest, Filter}
import com.amazonaws.services.kinesis.AmazonKinesisClient
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient
import com.amazonaws.util.EC2MetadataUtils
import com.gu.media.CrossAccountAccess
import play.api.Configuration

import scala.collection.JavaConverters._


class AWSConfig(val config: Configuration) extends CrossAccountAccess(config.underlying) {

  lazy val region: Region = {
    val r = config.getString("aws.region").map(Regions.fromName(_)).getOrElse(Regions.EU_WEST_1)
    Region.getRegion(r)
  }

  private val instanceProvider = InstanceProfileCredentialsProvider.getInstance()

  val credProvider: AWSCredentialsProvider = createCredentialProvider()
  val atomEventsProvider: AWSCredentialsProvider = getCrossAccountCredentials(credProvider, "media-atom-maker-atom-events")

  lazy val dynamoDB = region.createClient(
    classOf[AmazonDynamoDBClient],
    credProvider,
    null
  )

  lazy val ec2Client = region.createClient(
    classOf[AmazonEC2Client],
    credProvider,
    null
  )

  lazy val stage = config.getString("stage").getOrElse("DEV")

  lazy val composerUrl = config.getString("flexible.url").get

  lazy val dynamoTableName = config.getString("aws.dynamo.tableName").get
  lazy val publishedDynamoTableName = config.getString("aws.dynamo.publishedTableName").get
  lazy val auditDynamoTableName = config.getString("aws.dynamo.auditTableName").get

  lazy val liveKinesisStreamName = config.getString("aws.kinesis.liveStreamName").get
  lazy val previewKinesisStreamName = config.getString("aws.kinesis.previewStreamName").get

  lazy val previewKinesisReindexStreamName = config.getString("aws.kinesis.previewReindexStreamName").get
  lazy val publishedKinesisReindexStreamName = config.getString("aws.kinesis.publishedReindexStreamName").get

  lazy val userUploadBucket = config.getString("aws.upload.bucket").get
  lazy val userUploadFolder = config.getString("aws.upload.folder").get
  lazy val userUploadRole = config.getString("aws.upload.role").get

  lazy val readFromComposerAccount = config.getBoolean("readFromComposer").getOrElse(false)

  lazy val gridUrl = stage match {
    case "PROD" => "https://media.gutools.co.uk"
    case _ => "https://media.test.dev-gutools.co.uk"
  }

  lazy val kinesisClient = if (stage != "DEV" || readFromComposerAccount) {
    region.createClient(classOf[AmazonKinesisClient], atomEventsProvider, null)
  } else {
    region.createClient(classOf[AmazonKinesisClient], credProvider, null)
  }

  lazy val uploadSTSClient = createUploadSTSClient()

  lazy val expiryPollerName = "Expiry"
  lazy val expiryPollerLastName = "Poller"

  private def createUploadSTSClient() = {
    val provider = stage match {
      case "DEV" =>
        // Only required in dev. Instance profile credentials are sufficient when deployed
        val accessKey = config.getString("aws.upload.accessKey").getOrElse {
          throw new IllegalArgumentException("Missing aws.upload.accessKey. This is the AwsId output of the dev cloudformation")
        }

        val secretKey = config.getString("aws.upload.secretKey").getOrElse {
          throw new IllegalArgumentException("Missing aws.upload.secretKey. This is the AwsSecret output of the dev cloudformation")
        }

        new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey))

      case _ =>
        instanceProvider
    }

    region.createClient(classOf[AWSSecurityTokenServiceClient], provider, null)
  }

  private def createCredentialProvider() = {
    config.getString("aws.profile") match {
      case Some(profile) => new AWSCredentialsProviderChain(new ProfileCredentialsProvider(profile), instanceProvider)
      case None => instanceProvider
    }
  }

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
