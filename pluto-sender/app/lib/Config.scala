package com.gu.media.pluto


import com.amazonaws.regions.{RegionUtils, Region}
import com.amazonaws.auth.{BasicAWSCredentials, AWSCredentials}
import com.typesafe.config.Config
import com.gu.media.Settings
import com.gu.media.aws._


class PlutoSenderConfig(override val config: Config, val settings: Settings) extends AwsAccess {

    lazy val queueUrl = settings.getMandatoryString("uploaded.url")
    lazy val awsEndpoint = "endPoint"
    lazy val manualPlutoDynamo = "media-atom-maker-DEV-ManualPlutoMediaAtomsDynamoTable-1ANEPUQ8P8L4E"

}

