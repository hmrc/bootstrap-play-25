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

package uk.gov.hmrc.play.bootstrap.http

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import ch.qos.logback.classic.Level
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{LoneElement, Matchers, WordSpec}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.{Configuration, Logger}
import uk.gov.hmrc.auth.core.BearerTokenExpired
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, NotFoundException, Upstream5xxResponse}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.play.bootstrap.config.DummyRequestHeader
import uk.gov.hmrc.play.test.LogCapturing

import scala.concurrent.{ExecutionContext, Future}

trait MaterializerSupport {
  implicit val system: ActorSystem             = ActorSystem("Sys")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
}

class JsonErrorHandlerSpec
    extends WordSpec
    with Matchers
    with ScalaFutures
    with MockitoSugar
    with LoneElement
    with LogCapturing
    with Eventually
    with MaterializerSupport {

  "error handling in onError function" should {

    "convert a NotFoundException to NotFound response and audit the error" in new Setup {
      val resultF: Future[Result] = jsh.onServerError(requestHeader, new NotFoundException("test"))
      status(resultF) shouldEqual NOT_FOUND
      contentAsJson(resultF) shouldEqual Json.parse("""{"statusCode":404,"message":"test"}""")

      val captor = ArgumentCaptor.forClass(classOf[DataEvent])
      verify(auditConnector).sendEvent(captor.capture)(any[HeaderCarrier], any[ExecutionContext])
      captor.getValue.auditType shouldBe "ResourceNotFound"

    }

    "convert a BadRequestException to NotFound response and audit the error" in new Setup {
      val resultF: Future[Result] = jsh.onClientError(requestHeader, 400, "bad request")
      status(resultF) shouldEqual BAD_REQUEST
      contentAsJson(resultF) shouldEqual Json.parse("""{"statusCode":400,"message":"bad request"}""")

      val captor = ArgumentCaptor.forClass(classOf[DataEvent])
      verify(auditConnector).sendEvent(captor.capture)(any[HeaderCarrier], any[ExecutionContext])
      captor.getValue.auditType shouldBe "ServerValidationError"
    }

    "convert an UnauthorizedException to Unauthorized response and audit the error" in new Setup {
      val resultF: Future[Result] = jsh.onClientError(requestHeader, 401, "unauthorized")
      status(resultF) shouldEqual UNAUTHORIZED
      contentAsJson(resultF) shouldEqual Json.parse("""{"statusCode":401,"message":"unauthorized"}""")

      val captor = ArgumentCaptor.forClass(classOf[DataEvent])
      verify(auditConnector).sendEvent(captor.capture)(any[HeaderCarrier], any[ExecutionContext])
      captor.getValue.auditType shouldBe "ClientError"
    }

    "convert an AuthorisationException to Unauthorized response and audit the error" in new Setup {
      val resultF: Future[Result] = jsh.onServerError(requestHeader, new BearerTokenExpired)
      status(resultF) shouldEqual UNAUTHORIZED
      contentAsJson(resultF) shouldEqual Json.parse("""{"statusCode":401,"message":"Bearer token expired"}""")

      val captor = ArgumentCaptor.forClass(classOf[DataEvent])
      verify(auditConnector).sendEvent(captor.capture)(any[HeaderCarrier], any[ExecutionContext])
      captor.getValue.auditType shouldBe "ClientError"
    }

    "convert an Exception to InternalServerError and audit the error" in new Setup {
      val resultF: Future[Result] = jsh.onServerError(requestHeader, new Exception("any application exception"))
      status(resultF) shouldEqual INTERNAL_SERVER_ERROR
      contentAsJson(resultF) shouldEqual Json.parse("""{"statusCode":500,"message":"any application exception"}""")

      val captor = ArgumentCaptor.forClass(classOf[DataEvent])
      verify(auditConnector).sendEvent(captor.capture)(any[HeaderCarrier], any[ExecutionContext])
      captor.getValue.auditType shouldBe "ServerInternalError"
    }

    "log a warning for upstream code in the warning list" when {
      class WarningSetup(upstreamWarnStatuses: Seq[Int]) extends Setup {
        override val configuration: Configuration = Configuration(
          "appName" -> "myApp",
          "bootstrap.errorHandler.warnOnly.statusCodes" -> upstreamWarnStatuses
        )
      }

      "an UpstreamErrorResponse exception occurs" in new WarningSetup(Seq(500)) {
        withCaptureOfLoggingFrom(Logger) { logEvents =>
          jsh.onServerError(requestHeader, Upstream5xxResponse("any application exception", 500, 502)).futureValue

          eventually {
            val event = logEvents.loneElement
            event.getLevel   shouldBe Level.WARN
            event.getMessage shouldBe s"any application exception"
          }
        }
      }

      "a HttpException occurs" in new WarningSetup(Seq(400)) {
        withCaptureOfLoggingFrom(Logger) { logEvents =>
          jsh.onServerError(requestHeader, new BadRequestException("any application exception")).futureValue

          eventually {
            val event = logEvents.loneElement
            event.getLevel   shouldBe Level.WARN
            event.getMessage shouldBe s"any application exception"
          }
        }
      }
    }


    sealed trait Setup {
      val method        = "some-method"
      val uri           = "some-uri"
      val requestHeader = new DummyRequestHeader

      val auditConnector = mock[AuditConnector]
      when(auditConnector.sendEvent(any[DataEvent])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(Success))

      val configuration = Configuration("appName" -> "myApp")
      lazy val jsh           = new JsonErrorHandler(configuration, auditConnector)
    }
  }
}
