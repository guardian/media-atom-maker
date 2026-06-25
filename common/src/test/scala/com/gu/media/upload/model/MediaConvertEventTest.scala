package com.gu.media.upload.model

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.Json

class MediaConvertEventTest extends AnyFlatSpec with Matchers {

  "MediaConvertEvent" should "transparently deserialise and serialise an example event from AWS documentation" in {
    val json =
      """
        |{
        |    "version": "0",
        |    "id": "1234abcd-12ab-34cd-56ef-1234567890ab",
        |    "detail-type": "MediaConvert Job State Change",
        |    "source": "aws.mediaconvert",
        |    "account": "111122223333",
        |    "time": "2022-12-19T19:07:12Z",
        |    "region": "us-west-2",
        |    "resources": [
        |        "arn:aws:mediaconvert:us-west-2::jobs/1671476818694-phptj0"
        |    ],
        |    "detail": {
        |        "timestamp": 1671476832124,
        |        "accountId": "111122223333",
        |        "queue": "arn:aws:mediaconvert:us-west-2:111122223333:queues/Default",
        |        "jobId": "1671476818694-phptj0",
        |        "status": "COMPLETE",
        |        "userMetadata": {},
        |        "warnings": [
        |            {
        |                "code": 100000,
        |                "count": 1
        |            }
        |        ],
        |        "outputGroupDetails": [
        |            {
        |                "outputDetails": [
        |                    {
        |                        "outputFilePaths": [
        |                            "s3://amzn-s3-demo-bucket/file/file.mp4"
        |                        ],
        |                        "durationInMs": 30041,
        |                        "videoDetails": {
        |                            "widthInPx": 1920,
        |                            "heightInPx": 1080,
        |                            "qvbrAvgQuality": 7.38,
        |                            "qvbrMinQuality": 7,
        |                            "qvbrMaxQuality": 8,
        |                            "qvbrMinQualityLocation": 2168,
        |                            "qvbrMaxQualityLocation": 25025
        |                        }
        |                    }
        |                ],
        |                "type": "FILE_GROUP"
        |            }
        |        ],
        |        "paddingInserted": 0,
        |        "blackVideoDetected": 10,
        |        "blackSegments": [
        |            {
        |                "start": 0,
        |                "end": 10
        |            }
        |        ]
        |    }
        |}
        |""".stripMargin
    val parsedJson = Json.parse(json)
    Json.toJson(parsedJson.as[MediaConvertEvent]) shouldBe parsedJson
  }

  "MediaConvertEvent" should "transparently deserialise and serialise an real event" in {
    val json =
      """
        |{
        |  "version": "0",
        |  "id": "6024dfe7-0e47-8fcd-7cfc-c507ced492fe",
        |  "detail-type": "MediaConvert Job State Change",
        |  "source": "aws.mediaconvert",
        |  "account": "test",
        |  "time": "2026-02-23T13:10:59Z",
        |  "region": "eu-west-1",
        |  "resources": [
        |    "arn:aws:mediaconvert:eu-west-1:test:jobs/1771852248480-6dv3gn"
        |  ],
        |  "detail": {
        |    "timestamp": 1771852259573,
        |    "accountId": "test",
        |    "queue": "arn:aws:mediaconvert:eu-west-1:test:queues/Default",
        |    "jobId": "1771852248480-6dv3gn",
        |    "status": "COMPLETE",
        |    "userMetadata": {
        |      "stage": "DEV",
        |      "executionId": "arn:aws:states:eu-west-1:tes:execution:VideoPipelineDEV-PGZ5E0CNI0QG:f12cc481-7b54-4b4a-b7c5-af4cd93bb26f-1.14"
        |    },
        |    "outputGroupDetails": [
        |      {
        |        "outputDetails": [
        |          {
        |            "outputFilePaths": [
        |              "s3://uploads-origin.code.dev-guim.co.uk/2026/02/23/FFMPEG_Test--f12cc481-7b54-4b4a-b7c5-af4cd93bb26f-1.14.mp4"
        |            ],
        |            "durationInMs": 15932,
        |            "videoDetails": {
        |              "widthInPx": 900,
        |              "heightInPx": 720,
        |              "averageBitrate": 2405411,
        |              "qvbrAvgQuality": 7.14,
        |              "qvbrMinQuality": 7.0,
        |              "qvbrMaxQuality": 7.5,
        |              "qvbrMinQualityLocation": 0,
        |              "qvbrMaxQualityLocation": 792
        |            }
        |          },
        |          {
        |            "outputFilePaths": [
        |              "s3://uploads-origin.code.dev-guim.co.uk/2026/02/23/FFMPEG_Test--f12cc481-7b54-4b4a-b7c5-af4cd93bb26f-1.14.0000000.jpg"
        |            ],
        |            "durationInMs": 1000,
        |            "videoDetails": {
        |              "widthInPx": 1350,
        |              "heightInPx": 1080,
        |              "averageBitrate": 324268
        |            }
        |          }
        |        ],
        |        "type": "FILE_GROUP"
        |      },
        |      {
        |        "outputDetails": [
        |          {
        |            "outputFilePaths": [
        |              "s3://uploads-origin.code.dev-guim.co.uk/2026/02/23/FFMPEG_Test--f12cc481-7b54-4b4a-b7c5-af4cd93bb26f-1.14hls.m3u8"
        |            ],
        |            "durationInMs": 15932,
        |            "videoDetails": {
        |              "widthInPx": 900,
        |              "heightInPx": 720,
        |              "averageBitrate": 2411821,
        |              "qvbrAvgQuality": 7.17,
        |              "qvbrMinQuality": 7.0,
        |              "qvbrMaxQuality": 7.5,
        |              "qvbrMinQualityLocation": 83,
        |              "qvbrMaxQualityLocation": 542
        |            }
        |          },
        |          {
        |            "outputFilePaths": [
        |              "s3://uploads-origin.code.dev-guim.co.uk/2026/02/23/FFMPEG_Test--f12cc481-7b54-4b4a-b7c5-af4cd93bb26f-1.14captions.m3u8"
        |            ],
        |            "durationInMs": 0
        |          }
        |        ],
        |        "playlistFilePaths": [
        |          "s3://uploads-origin.code.dev-guim.co.uk/2026/02/23/FFMPEG_Test--f12cc481-7b54-4b4a-b7c5-af4cd93bb26f-1.14.m3u8"
        |        ],
        |        "type": "HLS_GROUP"
        |      }
        |    ],
        |    "paddingInserted": 0,
        |    "blackVideoDetected": 458,
        |    "blackVideoSegments": [
        |      {
        |        "start": 10635,
        |        "end": 11011
        |      },
        |      {
        |        "start": 11052,
        |        "end": 11136
        |      }
        |    ],
        |    "warnings": [
        |      {
        |        "code": 250003,
        |        "count": 1
        |      }
        |    ]
        |  }
        |}
        |
        |""".stripMargin
    val parsedJson = Json.parse(json)
    Json.toJson(parsedJson.as[MediaConvertEvent]) shouldBe parsedJson
  }

  "MediaConvertEvent" should "transparently deserialise and serialise an real event that does not have any warnings" in {
    val json =
      """
        |{
        |  "version": "0",
        |  "id": "6024dfe7-0e47-8fcd-7cfc-c507ced492fe",
        |  "detail-type": "MediaConvert Job State Change",
        |  "source": "aws.mediaconvert",
        |  "account": "test",
        |  "time": "2026-02-23T13:10:59Z",
        |  "region": "eu-west-1",
        |  "resources": [],
        |  "detail": {
        |    "timestamp": 1771852259573,
        |    "accountId": "test",
        |    "queue": "arn:aws:mediaconvert:eu-west-1:test:queues/Default",
        |    "jobId": "1771852248480-6dv3gn",
        |    "status": "COMPLETE",
        |    "userMetadata": {},
        |    "outputGroupDetails": [
        |      {
        |        "outputDetails": [],
        |        "type": "FILE_GROUP"
        |      }
        |    ],
        |    "paddingInserted": 0,
        |    "blackVideoDetected": 458,
        |    "blackVideoSegments": []
        |  }
        |}
        |""".stripMargin
    val parsedJson = Json.parse(json)
    Json.toJson(parsedJson.as[MediaConvertEvent]) shouldBe parsedJson
  }

  "MediaConvertEvent" should "transparently deserialise and serialise CMAF event which does not have outputFilePaths" in {
    val json =
      """{
        |  "version": "0",
        |  "id": "20b3a8db-8153-b68c-824e-6c970c8565c4",
        |  "detail-type": "MediaConvert Job State Change",
        |  "source": "aws.mediaconvert",
        |  "account": "563563610310",
        |  "time": "2026-06-16T15:26:14Z",
        |  "region": "eu-west-1",
        |  "resources": [
        |    "arn:aws:mediaconvert:eu-west-1:563563610310:jobs/1781623407306-4bov8h"
        |  ],
        |  "detail": {
        |    "timestamp": 1781623574418,
        |    "accountId": "563563610310",
        |    "queue": "arn:aws:mediaconvert:eu-west-1:563563610310:queues/Default",
        |    "jobId": "1781623407306-4bov8h",
        |    "status": "COMPLETE",
        |    "userMetadata": {
        |      "stage": "DEV",
        |      "executionId": "arn:aws:states:eu-west-1:563563610310:execution:VideoPipelineDEV-PGZ5E0CNI0QG:7da36eb3-442f-4b56-a411-0031956343d7-8",
        |      "hasAudio": "true"
        |    },
        |    "outputGroupDetails": [
        |      {
        |        "outputDetails": [
        |          {
        |            "outputFilePaths": [
        |              "s3://uploads-origin.code.dev-guim.co.uk/2026/06/16/WS_Change_tags_label--7da36eb3-442f-4b56-a411-0031956343d7-8.0_480w_q8_h264.mp4"
        |            ],
        |            "durationInMs": 84360,
        |            "videoDetails": {
        |              "widthInPx": 480,
        |              "heightInPx": 854,
        |              "averageBitrate": 1676481,
        |              "qvbrAvgQuality": 8.0,
        |              "qvbrMinQuality": 8.0,
        |              "qvbrMaxQuality": 8.0,
        |              "qvbrMinQualityLocation": 0,
        |              "qvbrMaxQualityLocation": 0
        |            }
        |          },
        |          {
        |            "outputFilePaths": [
        |              "s3://uploads-origin.code.dev-guim.co.uk/2026/06/16/WS_Change_tags_label--7da36eb3-442f-4b56-a411-0031956343d7-8.0_720h_q8_h264.mp4"
        |            ],
        |            "durationInMs": 84360,
        |            "videoDetails": {
        |              "widthInPx": 406,
        |              "heightInPx": 720,
        |              "averageBitrate": 1297659,
        |              "qvbrAvgQuality": 8.0,
        |              "qvbrMinQuality": 8.0,
        |              "qvbrMaxQuality": 8.0,
        |              "qvbrMinQualityLocation": 0,
        |              "qvbrMaxQualityLocation": 0
        |            }
        |          },
        |          {
        |            "outputFilePaths": [
        |              "s3://uploads-origin.code.dev-guim.co.uk/2026/06/16/WS_Change_tags_label--7da36eb3-442f-4b56-a411-0031956343d7-8.0.0000000.jpg"
        |            ],
        |            "durationInMs": 1000,
        |            "videoDetails": {
        |              "widthInPx": 1080,
        |              "heightInPx": 1920,
        |              "averageBitrate": 467499
        |            }
        |          },
        |          {
        |            "outputFilePaths": [
        |              "s3://uploads-origin.code.dev-guim.co.uk/2026/06/16/WS_Change_tags_label--7da36eb3-442f-4b56-a411-0031956343d7-8.0.vtt"
        |            ],
        |            "durationInMs": 0
        |          }
        |        ],
        |        "type": "FILE_GROUP"
        |      },
        |      {
        |        "outputDetails": [
        |          {
        |            "durationInMs": 84360,
        |            "videoDetails": {
        |              "widthInPx": 480,
        |              "heightInPx": 854,
        |              "averageBitrate": 1689527,
        |              "qvbrAvgQuality": 8.0,
        |              "qvbrMinQuality": 8.0,
        |              "qvbrMaxQuality": 8.0,
        |              "qvbrMinQualityLocation": 0,
        |              "qvbrMaxQualityLocation": 0
        |            }
        |          },
        |          {
        |            "durationInMs": 84360,
        |            "videoDetails": {
        |              "widthInPx": 406,
        |              "heightInPx": 720,
        |              "averageBitrate": 1306322,
        |              "qvbrAvgQuality": 8.0,
        |              "qvbrMinQuality": 8.0,
        |              "qvbrMaxQuality": 8.0,
        |              "qvbrMinQualityLocation": 0,
        |              "qvbrMaxQualityLocation": 0
        |            }
        |          },
        |          {
        |            "durationInMs": 84360,
        |            "videoDetails": {
        |              "widthInPx": 480,
        |              "heightInPx": 854,
        |              "averageBitrate": 607638,
        |              "qvbrAvgQuality": 8.0,
        |              "qvbrMinQuality": 7.86,
        |              "qvbrMaxQuality": 8.0,
        |              "qvbrMinQualityLocation": 79320,
        |              "qvbrMaxQualityLocation": 0
        |            }
        |          },
        |          {
        |            "durationInMs": 84360,
        |            "videoDetails": {
        |              "widthInPx": 406,
        |              "heightInPx": 720,
        |              "averageBitrate": 479797,
        |              "qvbrAvgQuality": 8.0,
        |              "qvbrMinQuality": 7.86,
        |              "qvbrMaxQuality": 8.0,
        |              "qvbrMinQualityLocation": 41800,
        |              "qvbrMaxQualityLocation": 0
        |            }
        |          },
        |          {
        |            "durationInMs": 84360,
        |            "videoDetails": {
        |              "widthInPx": 406,
        |              "heightInPx": 720,
        |              "averageBitrate": 572952,
        |              "qvbrAvgQuality": 6.0,
        |              "qvbrMinQuality": 6.0,
        |              "qvbrMaxQuality": 6.0,
        |              "qvbrMinQualityLocation": 0,
        |              "qvbrMaxQualityLocation": 0
        |            }
        |          },
        |          {
        |            "durationInMs": 84360,
        |            "videoDetails": {
        |              "widthInPx": 480,
        |              "heightInPx": 854,
        |              "averageBitrate": 715475,
        |              "qvbrAvgQuality": 6.0,
        |              "qvbrMinQuality": 6.0,
        |              "qvbrMaxQuality": 6.0,
        |              "qvbrMinQualityLocation": 0,
        |              "qvbrMaxQualityLocation": 0
        |            }
        |          },
        |          {
        |            "durationInMs": 84360,
        |            "videoDetails": {
        |              "widthInPx": 406,
        |              "heightInPx": 720,
        |              "averageBitrate": 262457,
        |              "qvbrAvgQuality": 4.0,
        |              "qvbrMinQuality": 4.0,
        |              "qvbrMaxQuality": 4.0,
        |              "qvbrMinQualityLocation": 0,
        |              "qvbrMaxQualityLocation": 0
        |            }
        |          },
        |          {
        |            "durationInMs": 84360,
        |            "videoDetails": {
        |              "widthInPx": 480,
        |              "heightInPx": 854,
        |              "averageBitrate": 323519,
        |              "qvbrAvgQuality": 4.0,
        |              "qvbrMinQuality": 4.0,
        |              "qvbrMaxQuality": 4.0,
        |              "qvbrMinQualityLocation": 0,
        |              "qvbrMaxQualityLocation": 0
        |            }
        |          },
        |          {
        |            "durationInMs": 0
        |          },
        |          {
        |            "durationInMs": 84358
        |          }
        |        ],
        |        "playlistFilePaths": [
        |          "s3://uploads-origin.code.dev-guim.co.uk/2026/06/16/WS_Change_tags_label--7da36eb3-442f-4b56-a411-0031956343d7-8.0.mpd",
        |          "s3://uploads-origin.code.dev-guim.co.uk/2026/06/16/WS_Change_tags_label--7da36eb3-442f-4b56-a411-0031956343d7-8.0.m3u8"
        |        ],
        |        "type": "CMAF_GROUP"
        |      }
        |    ],
        |    "paddingInserted": 0,
        |    "blackVideoDetected": 0,
        |    "warnings": [
        |      {
        |        "code": 250003,
        |        "count": 1
        |      }
        |    ]
        |  }
        |}
        |""".stripMargin
    val parsedJson = Json.parse(json)
    Json.toJson(parsedJson.as[MediaConvertEvent]) shouldBe parsedJson
  }
}
