package util

import com.amazonaws.auth.{ STSAssumeRoleSessionCredentialsProvider, AWSCredentialsProviderChain }
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.kinesis.AmazonKinesisClient
import play.api.Configuration
import javax.inject.{ Singleton, Inject }
import com.amazonaws.auth._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient


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

  lazy val kinesisArn = config.getString("aws.kinesis.streamArn").getOrElse("")

  lazy val atomsCredProvider = new AWSCredentialsProviderChain(
    new ProfileCredentialsProvider("composer"),
    new STSAssumeRoleSessionCredentialsProvider(credProvider, kinesisArn, sessionId)
  )

  lazy val dynamoDB = region.createClient(
    classOf[AmazonDynamoDBClient],
    credProvider,
    null
  )

  lazy val dynamoTableName = config.getString("aws.dynamo.tableName").get

  lazy val liveKinesisStreamName = config.getString("aws.kinesis.liveStreamName").get
  lazy val previewKinesisStreamName = config.getString("aws.kinesis.previewStreamName").get

  lazy val kinesisReindexStreamName = config.getString("aws.kinesis.reindexStreamName").get

  lazy val stage = config.getString("stage").getOrElse("DEV")
  lazy val readFromComposerAccount = config.getString("readFromComposer").getOrElse("false")

  lazy val kinesisClient = if (stage != "DEV" || readFromComposerAccount == "true")
    getKinesisClient(atomsCredProvider)
  else
    getKinesisClient(credProvider)

  private def getKinesisClient(credentialsProvider: AWSCredentialsProviderChain) = region.createClient(
    classOf[AmazonKinesisClient],
    credentialsProvider,
    null
  )
}
