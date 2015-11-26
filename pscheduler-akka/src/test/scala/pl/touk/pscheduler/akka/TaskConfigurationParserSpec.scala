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

import com.typesafe.config.ConfigFactory
import org.scalatest.{FlatSpec, Matchers}
import pl.touk.pscheduler.{Daily, EveryNDays}

import scala.concurrent.Future

class TaskConfigurationParserSpec extends FlatSpec with Matchers {

  it should "parse sample config" in {
    val sampleConfig =
      """schedule: [
        |  {
        |    name: foo
        |    schedule: every 3 days
        |  }
        |  {
        |    name: bar
        |    schedule: daily
        |  }
        |]
      """.stripMargin
    val config = ConfigFactory.parseString(sampleConfig)

    val parsed = TaskConfigurationParser.parse(config.getConfigList("schedule"), PartialFunction.apply(_ => () => Future.successful(Unit)))

    parsed should have length 2
    parsed(0).taskName shouldEqual "foo"
    parsed(0).schedule shouldEqual EveryNDays(3)
    parsed(1).taskName shouldEqual "bar"
    parsed(1).schedule shouldEqual Daily.atMidnight
  }

}