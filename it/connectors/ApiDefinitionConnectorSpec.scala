package connectors

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import models._
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.mvc.Http.Status
import utils.UnitSpec
import models.JsonFormatters._

class ApiDefinitionConnectorSpec extends UnitSpec with BeforeAndAfterAll with BeforeAndAfterEach {
  val port = 7001

  val playApplication = new GuiceApplicationBuilder()
    .configure("services.tapi-definition.port" -> "7001")
    .build()
  val wireMockServer = new WireMockServer(wireMockConfig().port(port))

  val api = APIDefinition("service", "http://service", "apiName", "apiDescription", "apiContext",
    Seq(APIVersion("v1", APIStatus.PUBLISHED, Seq(Endpoint("hello", "Hello World endpoint", HttpMethod.GET, AuthType.NONE)))))

  override def beforeAll {
    configureFor(port)
    wireMockServer.start()
  }

  override def afterAll: Unit = {
    wireMockServer.stop()
  }

  override def beforeEach(): Unit = {
    WireMock.reset()
  }

  trait Setup {
    val apiDefinitionConnector = playApplication.injector.instanceOf[ApiDefinitionConnector]
  }

  "createAPI" should {
    "create the API in tapi-definition" in new Setup {
      stubFor(post(urlPathEqualTo("/api-definition"))
        .withRequestBody(equalToJson(Json.toJson(api).toString()))
        .willReturn(aResponse()
          .withStatus(Status.NO_CONTENT)))

      val result = await(apiDefinitionConnector.createAPI(api))

      result shouldBe HasSucceeded
    }
  }
}