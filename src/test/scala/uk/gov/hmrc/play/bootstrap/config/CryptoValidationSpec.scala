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

package uk.gov.hmrc.play.bootstrap.config

import com.typesafe.config.ConfigFactory
import org.scalatest.{Matchers, WordSpec}
import play.api.Configuration
import uk.gov.hmrc.crypto.ApplicationCrypto

class CryptoValidationSpec extends WordSpec with Matchers {

  "CryptoValidation" must {

    "Be created successfully if crypto configuration is valid" in {
      import collection.JavaConverters._
      val confProperties = Map(
        "cookie.encryption.key"         -> "gvBoGdgzqG1AarzF1LY0zQ==",
        "queryParameter.encryption.key" -> "gvBoGdgzqG1AarzF1LY0zQ==",
        "sso.encryption.key"            -> "P5xsJ9Nt+quxGZzB4DeLfw=="
      )
      val config = Configuration(ConfigFactory.parseMap(confProperties.asJava))
      new CryptoValidation(new ApplicationCrypto(config.underlying))
    }

    "Fail if crypto configuration is invalid" in {
      val invalidConfiguration = new ApplicationCrypto(ConfigFactory.empty())
      an[RuntimeException] shouldBe thrownBy(new CryptoValidation(invalidConfiguration))
    }

  }

}
