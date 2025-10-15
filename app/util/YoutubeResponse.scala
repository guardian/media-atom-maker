package util

trait YoutubeResponse

class SuccesfulYoutubeResponse(val status: Option[String])
    extends YoutubeResponse
class YoutubeException(val exception: Throwable) extends YoutubeResponse
