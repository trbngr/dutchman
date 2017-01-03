package dutchman

import com.amazonaws.auth._
import dutchman.aws._
import dutchman.http._
import org.scalatest.{FlatSpec, Matchers}

class SignerSpecs extends FlatSpec with Matchers {
  val signer = AWSSigner(new BasicAWSCredentials("keyid", "key"), "us-west-2")

  "signing" should "work" in {
    val request = signer.sign(Endpoint.localhost, Request(GET, "/_bulk", Map("version" → "2")))
    val names = request.headers.map(_.name).toSet
    val pattern = "([A-Z0-9-]*) Credential=(.*), SignedHeaders=(.*), Signature=([a-z0-9]*)".r
    names should contain allOf("Authorization", "X-Amz-Date", "Host")

    val auth = request.headers.find(_.name == "Authorization").get

    auth.value match {
      case pattern(algo, cred, signedHeaders, sig) ⇒
        algo shouldBe AWSSigner.Algorithm
        cred should startWith("keyid")
        signedHeaders shouldBe "host;x-amz-date"
        sig shouldNot be(empty)
      case _ ⇒ fail("Authorization header didn't match expected pattern")
    }
  }
}
