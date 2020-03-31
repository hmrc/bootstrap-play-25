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

package uk.gov.hmrc.play.bootstrap.filters.frontend.deviceid

import java.util.Base64
import org.scalatest.{Matchers, WordSpec}
import play.api.mvc.Cookie
import play.api.test.FakeRequest

class DeviceFingerprintSpec extends WordSpec with Matchers {
  "deviceFingerprintFrom" should {
    "decode a base 64 encoded cookie" in {
      val request =
        FakeRequest().withCookies(
          Cookie(name = "mdtpdf", value = Base64.getEncoder.encodeToString("deviceFingerprintCookie".getBytes())))
      DeviceFingerprint.deviceFingerprintFrom(request) shouldBe "deviceFingerprintCookie"

    }

    "return '-' if the cookie is not encoded as valid base 64" in {
      val deviceFingerprint = new DeviceFingerprint(_ => throw new IllegalArgumentException("invalid base 64 string"))
      deviceFingerprint.deviceFingerprintFrom(FakeRequest()) shouldBe "-"
    }

  }
}
