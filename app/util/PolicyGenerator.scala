package util

import java.nio.charset.StandardCharsets
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDateTime, ZoneOffset}
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import com.google.common.io.BaseEncoding
import model.UploadPolicy
import play.api.libs.json._

object PolicyGenerator {
  def utf8(str: String): Array[Byte] = str.getBytes(StandardCharsets.UTF_8)
  def base64(str: String): String = BaseEncoding.base64().encode(utf8(str))

  def dateString(now: Instant): String = {
    // All time work is done in UTC
    LocalDateTime.ofInstant(now, ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMdd"))
  }

  def generate(bucket: String, key: String, accessKey: String, secretKey: String, region: String, now: Instant): UploadPolicy = {
    val now = Instant.now()
    val fiveMinutesFromNow = now.plus(5, ChronoUnit.HOURS)

    val endpoint: String = s"https://$bucket.s3.amazonaws.com"
    val credential = s"$accessKey/${dateString(now)}/$region/s3/aws4_request"
    val date = s"${dateString(now)}T000000Z"

    val json = toJson(date, fiveMinutesFromNow, bucket, key, credential)
    val encoded = base64(json)

    val signature = sign(base64(encoded), secretKey, region, now)

    null
  }

  def sign(base64Policy: String, secretKey: String, region: String, now: Instant): String = {
    val signingKey = generateSigningKey(secretKey, region, now)

    val result = hmac(signingKey, base64Policy)
    hex(result)
  }

  // http://docs.aws.amazon.com/general/latest/gr/signature-v4-examples.html#signature-v4-examples-java
  def generateSigningKey(secretKey: String, region: String, now: Instant): Array[Byte] = {
    val base = utf8(s"AWS4$secretKey")
    val steps = List(dateString(now), region, "s3", "aws4_request")

    steps.foldLeft(base)(hmac)
  }

  private def toJson(date: String, expirationTime: Instant, bucket: String, key: String, credential: String): String = {
    def exact(header: String, value: String) = JsObject(List(header -> js(value)))
    // This is the officially sanctioned syntax for "any value" (yes really)
    def anyValue(header: String) = JsArray(List(js("starts-with"), js('$' + header), js("")))
    def js(str: String): JsString = JsString(str)

    val jsonTree = JsObject(List(
      "expiration" -> js(expirationTime.toString), // ISO-8601
      "conditions" -> JsArray(List(
        exact("acl", "private"),
        exact("bucket", bucket),
        exact("key", key),
        exact("x-amz-algorithm", "AWS4-HMAC-SHA256"),
        exact("x-amz-credential", credential),
        exact("x-amz-date", date),
        anyValue("x-amz-meta-original")
      ))
    ))

    Json.stringify(jsonTree)
  }

  private def hmac(key: Array[Byte], data: String): Array[Byte] = {
    val algorithm = "HmacSHA256"
    val mac = Mac.getInstance(algorithm)
    mac.init(new SecretKeySpec(key, algorithm))

    mac.doFinal(utf8(data))
  }

  private def hex(bytes: Array[Byte]): String = BaseEncoding.base16().lowerCase().encode(bytes)
}
