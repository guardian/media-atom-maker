package util

import com.amazonaws.auth.{ STSAssumeRoleSessionCredentialsProvider, AWSCredentialsProviderChain }
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.kinesis.AmazonKinesisClient
import play.api.Configuration
import javax.inject.{ Singleton, Inject }
import com.amazonaws.auth._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.model.{DescribeTagsRequest, Filter}
import com.amazonaws.util.EC2MetadataUtils

import scala.collection.JavaConverters._


@Singleton
class AWSConfig @Inject() (config: Configuration) {

  lazy val region = {
    val r = config.getString("aws.region").map(Regions.fromName(_)).getOrElse(Regions.EU_WEST_1)
    Region.getRegion(r)
  }

  lazy val credProviders = Seq(
    config.getString("aws.profile").map(new ProfileCredentialsProvider(_)),
    Some(new InstanceProfileCredentialsProvider)
  )

  lazy val credProvider = new AWSCredentialsProviderChain(credProviders.flatten: _*)

  lazy val sessionId: String = "session" + Math.random()

  lazy val stsRoleToAssume = config.getString("aws.kinesis.stsRoleToAssume").getOrElse("")

  lazy val atomsCredProvider = new AWSCredentialsProviderChain(
    new ProfileCredentialsProvider("composer"),
    new STSAssumeRoleSessionCredentialsProvider(credProvider, stsRoleToAssume, sessionId)
  )

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

  lazy val composerUrl = config.getString("flexible.url").get

  lazy val dynamoTableName = config.getString("aws.dynamo.tableName").get
  lazy val publishedDynamoTableName = config.getString("aws.dynamo.publishedTableName").get
  lazy val auditDynamoTableName = config.getString("aws.dynamo.auditTableName").get

  lazy val liveKinesisStreamName = config.getString("aws.kinesis.liveStreamName").get
  lazy val previewKinesisStreamName = config.getString("aws.kinesis.previewStreamName").get

  lazy val previewKinesisReindexStreamName = config.getString("aws.kinesis.previewReindexStreamName").get
  lazy val publishedKinesisReindexStreamName = config.getString("aws.kinesis.publishedReindexStreamName").get

  lazy val loggingKinesisStreamName = config.getString("aws.kinesis.logging")

  lazy val stage = config.getString("stage").getOrElse("DEV")
  lazy val readFromComposerAccount = config.getBoolean("readFromComposer").getOrElse(false)

  lazy val gridUrl = stage match {
    case "PROD" => "https://media.gutools.co.uk"
    case _ => "https://media.test.dev-gutools.co.uk"
  }

  lazy val kinesisClient = if (stage != "DEV" || readFromComposerAccount)
    getKinesisClient(atomsCredProvider)
  else
    getKinesisClient(credProvider)

  lazy val expiryPollerName = "Expiry"
  lazy val expiryPollerLastName = "Poller"

  private def getKinesisClient(credentialsProvider: AWSCredentialsProviderChain) = region.createClient(
    classOf[AmazonKinesisClient],
    credentialsProvider,
    null
  )

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
