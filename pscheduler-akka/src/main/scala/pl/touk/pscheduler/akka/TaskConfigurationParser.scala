/*
 * Copyright 2015 the original author or authors.
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
package pl.touk.pscheduler.akka

import java.util

import com.typesafe.config.Config
import pl.touk.pscheduler.{Daily, EveryNDays, TaskConfiguration}

import scala.concurrent.Future

object TaskConfigurationParser {
  import collection.convert.wrapAsScala._

  def parse(configList: util.List[_ <: Config], handle: PartialFunction[String, () => Future[Unit]]): Seq[TaskConfiguration] = {
    configList.toIndexedSeq.flatMap { config =>
      val name = config.getString("name")
      handle.lift(name).map { job =>
        val EveryNDaysPattern = "every (\\d*) days".r("days")
        val schedule = config.getString("schedule") match {
          case EveryNDaysPattern(days) => EveryNDays(days.toInt)
          case "daily" => Daily.atMidnight
        }
        TaskConfiguration(name, schedule, job)
      }
    }
  }
}