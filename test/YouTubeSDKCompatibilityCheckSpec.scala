import com.google.api.client.googleapis.testing.auth.oauth2.MockGoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.http.{HttpRequestInitializer, HttpTransport}
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import org.scalatest.funspec.AnyFunSpec

class YouTubeSDKCompatibilityCheckSpec extends AnyFunSpec {

  val transport: HttpTransport = new NetHttpTransport()
  val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance
  val httpRequestInitializer: HttpRequestInitializer =
    new MockGoogleCredential.Builder().build()

  describe(
    "The YouTube SDK checks for a matching version of the google-api-client library"
  ) {
    it(
      "should NOT throw an exception creating a com.google.api.services.youtube.YouTube client"
    ) {
      new com.google.api.services.youtube.YouTube(
        transport,
        jsonFactory,
        httpRequestInitializer
      )
    }

    it(
      "should NOT throw an exception creating a com.google.api.services.youtubePartner.YouTubePartner client"
    ) {
      new com.google.api.services.youtubePartner.v1.YouTubePartner(
        transport,
        jsonFactory,
        httpRequestInitializer
      )
    }
  }
}
