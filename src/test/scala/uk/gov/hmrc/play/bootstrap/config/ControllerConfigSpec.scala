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

import org.scalatest.{MustMatchers, WordSpec}
import play.api.Configuration

class ControllerConfigSpec extends WordSpec with MustMatchers {

  "ControllerConfig.fromConfig" must {

    "return defaults when no config is found" in {

      val config = ControllerConfig.fromConfig(Configuration())

      config.auditing mustBe true
      config.logging mustBe true
    }

    "return defaults when config is set to defaults" in {

      val config = ControllerConfig.fromConfig(Configuration("needsAuditing" -> true, "needsLogging" -> true))

      config.auditing mustBe true
      config.logging mustBe true
    }

    "return all `false` when config is set to that" in {

      val config = ControllerConfig.fromConfig(Configuration("needsAuditing" -> false, "needsLogging" -> false))

      config.auditing mustBe false
      config.logging mustBe false
    }

  }

  "ControllerConfigs.fromConfig" must {

    val controllerConfigs = ControllerConfigs.fromConfig(Configuration(
      "controllers.a.b.c.foo.needsAuditing" -> false,
      "controllers.a.b.c.foo.needsLogging" -> false
    ))

    "return loaded configuration" in {

      val config = controllerConfigs.get("a.b.c.foo")

      config.auditing mustBe false
      config.logging mustBe false
    }

    "return default configuration for missing controllers" in {

      val config = controllerConfigs.get("bar")

      config.auditing mustBe true
      config.logging mustBe true
    }

    "not fail if there are primitive values with controllers. prefix" in {
      val controllerConfigsWithPrimitiveValues = ControllerConfigs.fromConfig(Configuration(
        "controllers.confidenceLevel" -> 300,
        "controllers.foo.needsAuditing" -> false,
        "controllers.foo.needsLogging" -> false
      ))

      val config = controllerConfigsWithPrimitiveValues.get("bar")

      config.auditing mustBe true
      config.logging mustBe true

    }
  }
}
