/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.play.bootstrap.auth

import javax.inject.Inject

import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.http.CorePost
import uk.gov.hmrc.play.bootstrap.config.BaseUrl
import uk.gov.hmrc.play.bootstrap.http.HttpClient

class DefaultAuthConnector @Inject() (
                                       httpClient: HttpClient,
                                       override val configuration: Configuration
                                     ) extends PlayAuthConnector with BaseUrl {

  override val serviceUrl: String = baseUrl("auth")

  override def http: CorePost = httpClient
}
