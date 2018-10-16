/*
 * Copyright 2018 HM Revenue & Customs
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

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.crypto.ApplicationCrypto

import collection.JavaConverters._

class CryptoValidationSpec extends WordSpec with Matchers {

  "CryptoValidation" must {

    "Be created successfully if crypto configuration is valid" in {
      val config: Config = ConfigFactory.parseMap(
        Map(
          "cookie.encryption.key" -> "gvBoGdgzqG1AarzF1LY0zQ==",
          "queryParameter.encryption.key" -> "gvBoGdgzqG1AarzF1LY0zQ==",
          "sso.encryption.key"            -> "P5xsJ9Nt+quxGZzB4DeLfw=="
        ).asJava
      )

      new CryptoValidation(new ApplicationCrypto(config))
    }

    "Fail if crypto configuration is invalid" in {
      an[RuntimeException] shouldBe thrownBy(new CryptoValidation(new ApplicationCrypto(ConfigFactory.empty())))
    }
  }

}
