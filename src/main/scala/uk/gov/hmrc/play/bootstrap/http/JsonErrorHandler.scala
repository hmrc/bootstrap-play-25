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

import javax.inject.Inject

import play.api.{Configuration, Logger}
import play.api.http.HttpErrorHandler
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}
import uk.gov.hmrc.auth.core.AuthorisationException
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.config.{AppName, HttpAuditEvent}
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

case class ErrorResponse(
  statusCode: Int,
  message: String,
  xStatusCode: Option[String] = None,
  requested: Option[String]   = None)

class JsonErrorHandler @Inject()(val configuration: Configuration, auditConnector: AuditConnector)
    extends HttpErrorHandler
    with HttpAuditEvent
    with AppName {

  /**
   * `upstreamWarnStatuses` is used to determine the log level for exceptions
   * relating to a HttpResponse. You can set this value in your config, with
   * the key `bootstrap.errorHandler.warnOnly.statusCodes`, as list of
   * integers representing response codes that should log at a warning level
   * rather an error level.
   *
   * e.g. bootstrap.errorHandler.warnOnly.statusCodes=[400,404,502]
   *
   * This is used to reduce the number of noise the number of duplicated alerts
   * for a microservice.
   */
  protected val upstreamWarnStatuses: Seq[Int] = configuration.getIntSeq("bootstrap.errorHandler.warnOnly.statusCodes").getOrElse(Nil).map(_.intValue())
  implicit val erFormats = Json.format[ErrorResponse]

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {

    implicit val headerCarrier = HeaderCarrierConverter.fromHeadersAndSessionAndRequest(request.headers, request = Some(request))

    statusCode match {
      case play.mvc.Http.Status.NOT_FOUND =>
        auditConnector.sendEvent(dataEvent("ResourceNotFound", "Resource Endpoint Not Found", request))
        Future.successful(
          NotFound(Json.toJson(ErrorResponse(NOT_FOUND, "URI not found", requested = Some(request.path)))))
      case play.mvc.Http.Status.BAD_REQUEST =>
        auditConnector.sendEvent(dataEvent("ServerValidationError", "Request bad format exception", request))
        Future.successful(BadRequest(Json.toJson(ErrorResponse(BAD_REQUEST, "bad request"))))
      case _ =>
        auditConnector.sendEvent(dataEvent("ClientError", s"A client error occurred, status: $statusCode", request))
        Future.successful(Status(statusCode)(Json.toJson(ErrorResponse(statusCode, message))))
    }
  }

  override def onServerError(request: RequestHeader, ex: Throwable) = {
    implicit val headerCarrier = HeaderCarrierConverter.fromHeadersAndSessionAndRequest(request.headers, request = Some(request))

    val message = s"! Internal server error, for (${request.method}) [${request.uri}] -> "

    val code = ex match {
      case _: NotFoundException       => "ResourceNotFound"
      case _: AuthorisationException  => "ClientError"
      case _: JsValidationException   => "ServerValidationError"
      case _                          => "ServerInternalError"
    }

    val errorResponse = ex match {
      case e: AuthorisationException =>
        Logger.error(message, e)
        ErrorResponse(401, e.getMessage)
      case e: HttpException =>
        logException(e, e.responseCode)
        ErrorResponse(e.responseCode, e.getMessage)
      case e: Exception with UpstreamErrorResponse =>
        logException(e, e.upstreamResponseCode)
        ErrorResponse(e.reportAs, e.getMessage)
      case e: Throwable =>
        Logger.error(message, e)
        ErrorResponse(INTERNAL_SERVER_ERROR, e.getMessage)
    }

    auditConnector.sendEvent(
      dataEvent(code, "Unexpected error", request, Map("transactionFailureReason" -> ex.getMessage)))
    Future.successful(new Status(errorResponse.statusCode)(Json.toJson(errorResponse)))
  }

  private def logException(exception: Exception, responseCode: Int): Unit = {
    if(upstreamWarnStatuses contains responseCode)
      Logger.warn(exception.getMessage, exception)
    else
      Logger.error(exception.getMessage, exception)
  }
}
