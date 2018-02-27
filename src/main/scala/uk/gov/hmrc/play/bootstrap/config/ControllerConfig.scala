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

import com.typesafe.config.ConfigObject
import play.api.Configuration

import scala.util.Try

case class ControllerConfig(logging: Boolean = true, auditing: Boolean = true)

object ControllerConfig {

  def fromConfig(configuration: Configuration): ControllerConfig = {
    val logging = configuration.getBoolean("needsLogging").getOrElse(true)
    val auditing = configuration.getBoolean("needsAuditing").getOrElse(true)
    ControllerConfig(logging, auditing)
  }
}

case class ControllerConfigs(private val controllers: Map[String, ControllerConfig]) {

  def get(controllerName: String): ControllerConfig =
    controllers.getOrElse(controllerName, ControllerConfig())
}

object ControllerConfigs {

  def fromConfig(configuration: Configuration): ControllerConfigs = {

    val configMap = (
      for {
        configs <- configuration.getConfig("controllers").toSeq
        key <- subpaths(configs)
        entryForController <- readCompositeValue(configs, key)
        parsedEntryForController = ControllerConfig.fromConfig(entryForController)
      } yield (key, parsedEntryForController)
      ).toMap

    ControllerConfigs(configMap)
  }

  private def subpaths(c: Configuration): List[String] = {

    def loop(config: Configuration, acc: List[List[String]]): List[List[String]] = {
      val subkeys = config.subKeys.toList

      if (subkeys.isEmpty) {
        acc
      } else {
        subkeys.flatMap { key ⇒
          Try(config.getConfig(key)).toOption.flatten match {
            case None ⇒
              acc

            case Some(c) ⇒
              val next: List[List[String]] = acc match {
                case Nil ⇒ List(List(key))
                case l ⇒ l.map(key :: _)
              }

              loop(c, next)
          }
        }
      }
    }

    loop(c, List.empty[List[String]]).map(_.reverse.mkString("."))
  }

  private def readCompositeValue(configuration : Configuration, key : String) : Option[Configuration] = {
    if (configuration.underlying.hasPathOrNull(key)) {
      configuration.underlying.getValue(key) match {
        case o : ConfigObject => Some(Configuration(o.toConfig))
        case _ => None
      }
    } else None
  }
}