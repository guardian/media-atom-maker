package util

import com.amazonaws.regions.{Region, Regions}
import play.api.Configuration
import javax.inject.{ Singleton, Inject }
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient 


@Singleton
class AWS @Inject() (config: Configuration) {

  lazy val region = {
    val r = config.getString("aws.region").map(Regions.fromName(_)).getOrElse(Regions.EU_WEST_1)
    Region.getRegion(r)
  }

  lazy val credProfile = config.getString("aws.profile").get

  lazy val dynamoDB = region.createClient(
    classOf[AmazonDynamoDBClient],
    new ProfileCredentialsProvider(credProfile),
    null
  )

  lazy val dynamoTableName = config.getString("aws.dynamo.tableName").get

}
