package integration.services

trait TestAtomJsonGenerator {

  def generateJson(title: String, description: String, category: String, channelId: String, youtubeCategoryId: String, expiryDate: Long): String = {
    s"""{
      "title": "$title",
      "description": "$description",
      "category": "$category",
      "duration": 0,
      "channelId": "$channelId",
      "youtubeCategoryId": "$youtubeCategoryId",
      "privacyStatus": "Unlisted",
      "youtubeCategory": "$youtubeCategoryId",
      "youtubeChannel": "$channelId",
      "expiryDate": $expiryDate,
      "tags": [],
      "byline": [],
      "commissioningDesks": [],
      "keywords": [],
      "commentsEnabled": false,
      "blockAds": false,
      "isLoopingVideo": false,
      "contentChangeDetails": {
        "revision": 0
      },
      "posterImage": {
        "assets": [
          {
            "file": "https://s3-eu-west-1.amazonaws.com/media-origin.test.dev-guim.co.uk/0c2bf48b4d73bb97551ef41caa5451939a052ed3/0_0_3500_2099/2000.jpg",
            "mimeType": "image/jpeg",
            "size": 213759,
            "dimensions": {
              "width": 2000,
              "height": 1200
            }
          },
          {
            "file": "https://s3-eu-west-1.amazonaws.com/media-origin.test.dev-guim.co.uk/0c2bf48b4d73bb97551ef41caa5451939a052ed3/0_0_3500_2099/1000.jpg",
            "mimeType": "image/jpeg",
            "size": 76889,
            "dimensions": {
              "width": 1000,
              "height": 600
            }
          },
          {
            "file": "https://s3-eu-west-1.amazonaws.com/media-origin.test.dev-guim.co.uk/0c2bf48b4d73bb97551ef41caa5451939a052ed3/0_0_3500_2099/500.jpg",
            "mimeType": "image/jpeg",
            "size": 29398,
            "dimensions": {
              "width": 500,
              "height": 300
            }
          },
          {
            "file": "https://s3-eu-west-1.amazonaws.com/media-origin.test.dev-guim.co.uk/0c2bf48b4d73bb97551ef41caa5451939a052ed3/0_0_3500_2099/140.jpg",
            "mimeType": "image/jpeg",
            "size": 6989,
            "dimensions": {
              "width": 140,
              "height": 84
            }
          },
          {
            "file": "https://s3-eu-west-1.amazonaws.com/media-origin.test.dev-guim.co.uk/0c2bf48b4d73bb97551ef41caa5451939a052ed3/0_0_3500_2099/3500.jpg",
            "mimeType": "image/jpeg",
            "size": 602837,
            "dimensions": {
              "width": 3500,
              "height": 2099
            }
          }
        ],
        "master": {
          "file": "https://s3-eu-west-1.amazonaws.com/media-origin.test.dev-guim.co.uk/0c2bf48b4d73bb97551ef41caa5451939a052ed3/0_0_3500_2099/master/3500.jpg",
          "mimeType": "image/jpeg",
          "size": 2569265,
          "dimensions": {
            "width": 3500,
            "height": 2099
          }
        },
        "mediaId": "https://api.media.test.dev-gutools.co.uk/images/0c2bf48b4d73bb97551ef41caa5451939a052ed3"
      }
    }"""
  }

  def generateUploadRequest(atomId: String, size: Long): String =
    s"""
      {
        "atomId": "$atomId",
        "filename": "test",
        "size": $size,
        "selfHost": false,
        "syncWithPluto": false
      }
    """

}
