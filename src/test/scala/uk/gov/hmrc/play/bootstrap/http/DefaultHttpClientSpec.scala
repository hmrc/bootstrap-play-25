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

import com.github.tomakehurst.wiremock.client.WireMock._
import org.joda.time.LocalDate
import org.scalatest.TestData
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.play.OneAppPerTest
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.http.utils._
import uk.gov.hmrc.play.test.UnitSpec

class DefaultHttpClientSpec
    extends UnitSpec
    with ScalaFutures
    with IntegrationPatience
    with WiremockTestServer
    with OneAppPerTest {

  override def newAppForTest(testData: TestData): Application =
    new GuiceApplicationBuilder()
      .overrides(
        bind[HttpClient].to[DefaultHttpClient],
        bind[AuditConnector].to[TestAuditConnector]
      )
      .build()

  def myHttpClient = app.injector.instanceOf[HttpClient]

  "A GET" should {

    import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
    implicit val hc    = HeaderCarrier()
    implicit val reads = BankHolidays.reads

    "read some json and return a case class" in {

      stubFor(
        get("/bank-holidays.json")
          .willReturn(ok(JsonPayloads.bankHolidays)))

      val bankHolidays: BankHolidays =
        myHttpClient.GET[BankHolidays]("http://localhost:20001/bank-holidays.json").futureValue
      bankHolidays.events.head shouldBe BankHoliday("New Year's Day", new LocalDate(2017, 1, 2))
    }

    "read some json and return a raw http response" in {

      stubFor(
        get("/bank-holidays.json")
          .willReturn(ok(JsonPayloads.bankHolidays)))

      val response: HttpResponse = myHttpClient.GET("http://localhost:20001/bank-holidays.json").futureValue
      response.status shouldBe 200
      response.body   shouldBe JsonPayloads.bankHolidays
    }

    "be able to handle a 404 without throwing an exception" in {

      stubFor(
        get("/404.json")
          .willReturn(notFound))

      // By adding an Option to your case class, the 404 is translated into None
      val bankHolidays: Option[BankHolidays] =
        myHttpClient.GET[Option[BankHolidays]]("http://localhost:20001/404.json").futureValue
      bankHolidays shouldBe None
    }

    "be able to handle an empty body on 204" in {

      stubFor(
        get("/204.json")
          .willReturn(noContent))

      // By adding an Option to your case class, the 204 is translated into None
      val bankHolidays = myHttpClient.GET[Option[BankHolidays]]("http://localhost:20001/204.json").futureValue
      bankHolidays shouldBe None
    }

    "throw an BadRequestException for 400 errors" in {

      stubFor(
        get("/400.json")
          .willReturn(badRequest))

      myHttpClient
        .GET[Option[BankHolidays]]("http://localhost:20001/400.json")
        .recover {
          case e: BadRequestException => // handle here a bad request
        }
        .futureValue
    }

    "throw an Upstream4xxResponse for 4xx errors" in {

      stubFor(
        get("/401.json")
          .willReturn(unauthorized))

      myHttpClient
        .GET[Option[BankHolidays]]("http://localhost:20001/401.json")
        .recover {
          case e: Upstream4xxResponse => // handle here a 4xx errors
        }
        .futureValue
    }

    "throw an Upstream5xxResponse for 4xx errors" in {

      stubFor(
        get("/500.json")
          .willReturn(serverError))

      myHttpClient
        .GET[Option[BankHolidays]]("http://localhost:20001/500.json")
        .recover {
          case e: Upstream5xxResponse => // handle here a 5xx errors
        }
        .futureValue
    }
  }

  "A POST" should {

    import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
    implicit val hc  = HeaderCarrier()
    implicit val uw  = User.writes
    implicit val uir = UserIdentifier.reads

    "write a case class to json body and return a response" in {

      stubFor(
        post("/create-user")
          .willReturn(noContent))
      val user = User("me@mail.com", "John Smith")

      // Use HttpResponse when the API always returns an empty body
      val response: HttpResponse =
        myHttpClient.POST[User, HttpResponse]("http://localhost:20001/create-user", user).futureValue
      response.status shouldBe 204
    }

    "read the response body of the POST into a case class" in {

      stubFor(
        post("/create-user")
          .willReturn(ok(JsonPayloads.userId)))
      val user = User("me@mail.com", "John Smith")

      // Use a case class when the API returns a json body
      val userId: UserIdentifier =
        myHttpClient.POST[User, UserIdentifier]("http://localhost:20001/create-user", user).futureValue
      userId.id shouldBe "123"
    }

    "be able to handle both 204 and 200 in the same configuration" in {

      stubFor(
        post("/create-user")
          .willReturn(noContent))
      val user = User("me@mail.com", "John Smith")

      // Use Option[T], where T is your case class, if the API might return both 200 and 204
      val userId: Option[UserIdentifier] =
        myHttpClient.POST[User, Option[UserIdentifier]]("http://localhost:20001/create-user", user).futureValue
      userId shouldBe None
    }

  }
}
