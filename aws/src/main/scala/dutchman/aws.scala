package dutchman

import java.net.{URL, URLEncoder}
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.{Calendar, TimeZone}
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import com.amazonaws.auth._
import dutchman.http._

object aws {

  object AWSSigner {
    val Algorithm = "AWS4-HMAC-SHA256"
    def apply(credentials: AWSCredentials, region: String) = new AWSSigner(new AWSStaticCredentialsProvider(credentials), region, "es")
    def apply(credentials: AWSCredentialsProvider, region: String): AWSSigner = new AWSSigner(credentials, region, "es")
  }

  implicit class RequestUrlBuilder(request: Request){
    def url(endpoint: Endpoint) = {
      val query = for {
        (name, value) <- request.params
        encodedValue = URLEncoder.encode(value, "UTF8")
      } yield name + "=" + encodedValue

      new URL(endpoint.protocol, endpoint.host, endpoint.port, s"${request.path}${query.mkString("?", "&", "")}")
    }
  }

  final class AWSSigner(credentials: AWSCredentialsProvider, region: String, service: String)
    extends ESRequestSigner {

    import AWSSigner._

    require(credentials.getCredentials != null, "credentials provider must return non null credentials.")

    private def awsCredentials: AWSCredentials = credentials.getCredentials

    def sign(endpoint: Endpoint, request: Request): Request = {
      val (dateTimeStr, dateStr) = currentDateStrings

      val url = request.url(endpoint)

      val withDateAndHost = completedRequest(url, request, dateTimeStr)

      val headerValue = s"$Algorithm Credential=${awsCredentials.getAWSAccessKeyId}/${credentialScope(dateStr)}, SignedHeaders=${signedHeaders(withDateAndHost)}" +
        s", Signature=${signature(url, withDateAndHost, dateTimeStr, dateStr)}"

      // Append the session key, if session credentials were provided
      addSessionToken(withDateAndHost.copy(
        headers = Header("Authorization", headerValue) +: withDateAndHost.headers
      ))
    }

    val xAmzSecurityToken = "X-Amz-Security-Token"

    def addSessionToken(request: Request): Request = awsCredentials match {
      case (sc: AWSSessionCredentials) ⇒ request.copy(
        headers = Header(xAmzSecurityToken, sc.getSessionToken) +: request.headers
      )
      case _                           ⇒ request
    }

    private[aws] def completedRequest(url: URL, request: Request, dateTimeStr: String): Request = {
      val headers = request.headers
      // Add a date and host header, but only if they aren't already there
      val dateHeader = if (headers.exists(header ⇒ Set(xAmzDate.toLowerCase, "date").contains(header.name.toLowerCase))) {
        Seq()
      } else {
        Seq(Header(xAmzDate, dateTimeStr))
      }

      val hostHeader = if (headers.exists(_.name.toLowerCase == "host")) {
        Seq()
      } else {
        Seq(Header("Host", url.getHost))
      }
      request.copy(
        headers = dateHeader ++ hostHeader ++ headers
      )
    }

    private[aws] def stringToSign(url: URL, request: Request, dateTimeStr: String, dateStr: String): String = {
      s"$Algorithm\n" +
        s"$dateTimeStr\n" +
        s"${credentialScope(dateStr)}\n" +
        s"${canonicalRequestHash(url, request)}"
    }

    private[aws] def createCanonicalRequest(url: URL, request: Request): String = {
      val payload = hashedPayloadByteArray(request)
      f"${request.verb.toString}\n" +
        f"${canonicalUri(url)}\n" +
        f"${canonicalQueryString(url)}\n" +
        f"${canonicalHeaders(request)}\n" +
        f"${signedHeaders(request)}\n" +
        f"${hexOf(payload)}"
    }

    // Protected so that it can be overridden in tests
    protected def currentDateStrings: (String, String) = {
      val cal = Calendar.getInstance()
      val dfmT = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'")
      dfmT.setTimeZone(TimeZone.getTimeZone("GMT"))
      val dateTimeStr = dfmT.format(cal.getTime)
      val dfmD = new SimpleDateFormat("yyyyMMdd")
      dfmD.setTimeZone(TimeZone.getTimeZone("GMT"))
      val dateStr = dfmD.format(cal.getTime)
      (dateTimeStr, dateStr)
    }

    val xAmzDate = "X-Amz-Date"

    private def signature(url: URL, request: Request, dateTimeStr: String, dateStr: String): String = {
      val signature = hmacSHA256(stringToSign(url, request, dateTimeStr, dateStr), signingKey(dateStr))
      hexOf(signature)
    }

    private def signingKey(dateStr: String): Array[Byte] = {
      val kSecret = ("AWS4" + awsCredentials.getAWSSecretKey).getBytes("UTF8")
      val kDate = hmacSHA256(dateStr, kSecret)
      val kRegion = hmacSHA256(region, kDate)
      val kService = hmacSHA256(service, kRegion)
      hmacSHA256("aws4_request", kService)
    }

    private def credentialScope(dateStr: String) = {
      s"$dateStr/$region/$service/aws4_request"
    }

    private def canonicalRequestHash(url: URL, request: Request) = {
      hexOf(hashSha256(createCanonicalRequest(url, request)))
    }

    private def canonicalUri(url: URL): String = {
      val segments = url.getPath.split("/")
      segments.filter(_ != "").map(urlEncode).mkString(start = "/", sep = "/", end = "")
    }

    private def urlEncode(s: String): String = URLEncoder.encode(s, "utf-8")

    private def canonicalQueryString(url: URL): String = {
      if (url.getQuery.isEmpty) {
        ""
      } else {
        val params = url.getQuery.split("&") map { x ⇒
          val parts = x.split("=")
          (parts(0), parts(1))
        }
        val sortedEncoded = params.toList.map(kv ⇒ (urlEncode(kv._1), urlEncode(kv._2))).sortBy(_._1)
        sortedEncoded.map(kv ⇒ s"${kv._1}=${kv._2}").mkString("&")
      }
    }

    private def canonicalHeaders(request: Request): String = {
      request.headers.sortBy(_.name.toLowerCase).map(header ⇒ s"${header.name.toLowerCase}:${header.value.trim}").mkString("\n") + "\n"
    }

    private def signedHeaders(request: Request): String = {
      request.headers.map(_.name.toLowerCase).sorted.mkString(";")
    }

    private def hashedPayloadByteArray(request: Request): Array[Byte] = {
      hashSha256(request.payload)
    }

    private def hashSha256(v: String): Array[Byte] = {
      val md = MessageDigest.getInstance("SHA-256")
      md.update(v.getBytes("UTF-8"))
      val digest = md.digest()
      digest
    }

    private def hmacSHA256(data: String, key: Array[Byte]): Array[Byte] = {
      val algorithm = "HmacSHA256"
      val mac = Mac.getInstance(algorithm)
      mac.init(new SecretKeySpec(key, algorithm))
      mac.doFinal(data.getBytes("UTF8"))
    }

    private def hexOf(buf: Array[Byte]) = buf.map("%02X" format _).mkString.toLowerCase
  }
}
