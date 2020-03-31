/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.play.bootstrap.logging
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterEach, MustMatchers, OptionValues, WordSpec}
import org.slf4j.MDC
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.{Action, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderNames => HMRCHeaderNames}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

class MDCLoggingSpec extends WordSpec with MustMatchers with ScalaFutures with OptionValues with BeforeAndAfterEach {

  override def beforeEach(): Unit =
    MDC.clear()

  override def afterEach(): Unit =
    MDC.clear()

  def anApplicationWithMDCLogging(configFile: String): Unit = {

    val config = Configuration(ConfigFactory.load(configFile))

    "must pass MDC information between thread contexts" in {

      lazy val app = new GuiceApplicationBuilder()
        .configure(config)
        .build()

      running(app) {

        implicit val ec: ExecutionContext =
          app.injector.instanceOf[ExecutionContext]

        MDC.put("foo", "bar")

        val future = Future {
          Option(MDC.get("foo"))
        }

        whenReady(future) {
          _.value mustEqual "bar"
        }
      }
    }

    "foobar" in {

      lazy val router = {

        import play.api.routing._
        import play.api.routing.sird._

        Router.from {
          case GET(p"/") =>
            Action {
              Results.Ok {
                Json.toJson {
                  Option(MDC.getCopyOfContextMap)
                    .map(_.asScala)
                    .getOrElse(Map.empty[String, String])
                }
              }
            }
        }
      }

      lazy val app = new GuiceApplicationBuilder()
        .configure(config)
        .router(router)
        .build()

      running(app) {

        val request = FakeRequest(GET, "/")
          .withHeaders(
            HMRCHeaderNames.xSessionId    -> "some session id",
            HMRCHeaderNames.xRequestId    -> "some request id",
            HMRCHeaderNames.xForwardedFor -> "some forwarded for"
          )

        val result = route(app, request).value

        status(result) mustBe OK

        val mdc = contentAsJson(result).as[Map[String, String]]

        mdc                             must contain allOf (
          "appName"                     -> "test-application",
          "logger.json.dateformat"      -> "YYYY-mm-DD",
          HMRCHeaderNames.xSessionId    -> "some session id",
          HMRCHeaderNames.xRequestId    -> "some request id",
          HMRCHeaderNames.xForwardedFor -> "some forwarded for"
        )
      }
    }
  }

  "a microservice" must {
    behave like anApplicationWithMDCLogging("microservice.test.conf")
  }

  "a frontend" must {
    behave like anApplicationWithMDCLogging("frontend.test.conf")
  }
}
