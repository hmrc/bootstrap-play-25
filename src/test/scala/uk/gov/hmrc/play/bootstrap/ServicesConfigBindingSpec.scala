/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.play.bootstrap

import com.typesafe.config.ConfigFactory
import org.scalatest.{MustMatchers, WordSpec}
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.play.config.ServicesConfig

class ServicesConfigBindingSpec extends WordSpec with MustMatchers {

  def anApplicationWithBoundedServicesConfig(configFile: String): Unit = {
    lazy val app = new GuiceApplicationBuilder()
      .configure(Configuration(ConfigFactory.load(configFile)))
      .build()

    "bind ServicesConfig in default configuration" in {
      app.injector.instanceOf[ServicesConfig] must not be null
    }

    "have forward compatible with bootstrap-play-26" in {
      app.injector.instanceOf[uk.gov.hmrc.play.bootstrap.config.ServicesConfig] must not be null
    }
  }

  "A frotnend service" must {
    behave like anApplicationWithBoundedServicesConfig("frontend.test.conf")
  }

  "A microservice" must {
    behave like anApplicationWithBoundedServicesConfig("microservice.test.conf")
  }
}
