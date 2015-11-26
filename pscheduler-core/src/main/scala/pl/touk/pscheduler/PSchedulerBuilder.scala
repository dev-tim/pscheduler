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
package pl.touk.pscheduler

import java.time.Duration

import scala.language.postfixOps

class PSchedulerBuilder[+P, +CS, +CI, +C](private[pscheduler] val definedPersistence: Option[TasksPersistence],
                                          private[pscheduler] val definedCheckScheduler: Option[InMemoryScheduler],
                                          private[pscheduler] val definedCheckInterval: Option[Duration],
                                          private[pscheduler] val definedConfiguration: Option[Seq[TaskConfiguration]]) {
  
  def withPersistence(persistence: TasksPersistence): PSchedulerBuilder[Defined, CS, CI, C] =
    new PSchedulerBuilder(Some(persistence), definedCheckScheduler, definedCheckInterval, definedConfiguration)

  def withCheckScheduler(checkScheduler: InMemoryScheduler): PSchedulerBuilder[P, Defined, CI, C] =
    new PSchedulerBuilder(definedPersistence, Some(checkScheduler), definedCheckInterval, definedConfiguration)

  def withCheckInterval(checkInterval: Duration): PSchedulerBuilder[P, CS, Defined, C] =
    new PSchedulerBuilder(definedPersistence, definedCheckScheduler, Some(checkInterval), definedConfiguration)

  def withTasks(configuration: TaskConfiguration*): PSchedulerBuilder[P, CS, CI, Defined] =
    new PSchedulerBuilder(definedPersistence, definedCheckScheduler, definedCheckInterval, Some(configuration))
  
}

trait Defined

object PSchedulerBuilder extends PSchedulerBuilder(None, None, None, None) {

  implicit class FullyConfigured(builder: PSchedulerBuilder[Defined, Defined, Defined, Defined]) {
    def build: PScheduler = new PScheduler(
      persistence = builder.definedPersistence.get,
      checkScheduler = builder.definedCheckScheduler.get,
      checkInterval = builder.definedCheckInterval.get,
      configuration = builder.definedConfiguration.get
    )
  }
}