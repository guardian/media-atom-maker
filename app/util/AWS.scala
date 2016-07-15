package util

import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.kinesis.AmazonKinesisClient
import play.api.Configuration
import javax.inject.{ Singleton, Inject }
import com.amazonaws.auth.{ AWSCredentialsProviderChain, InstanceProfileCredentialsProvider }
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

  lazy val dynamoDB = region.createClient(
    classOf[AmazonDynamoDBClient],
    credProvider,
    null
  )

  lazy val dynamoTableName = config.getString("aws.dynamo.tableName").get

  lazy val kinesisStreamName = config.getString("aws.kinesis.streamName").get

  lazy val kinesisReindexStreamName = config.getString("aws.kinesis.reindexStreamName").get

  lazy val kinesisClient = region.createClient(
    classOf[AmazonKinesisClient],
    credProvider,
    null
  )
}
