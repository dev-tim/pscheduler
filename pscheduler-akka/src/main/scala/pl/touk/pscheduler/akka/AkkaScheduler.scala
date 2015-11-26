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

import java.time.Duration
import java.util.concurrent.TimeUnit

import pl.touk.pscheduler.{Cancellable, InMemoryScheduler}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration

class AkkaScheduler(scheduler: akka.actor.Scheduler) extends InMemoryScheduler {
  override def schedule(job: => Future[Unit], interval: Duration)
                       (implicit ec: ExecutionContext): Cancellable = {
    val scheduledJob = scheduler.scheduleOnce(FiniteDuration(interval.toMillis, TimeUnit.MILLISECONDS))(job)
    new Cancellable {
      override def cancel(): Unit = scheduledJob.cancel()
    }
  }
}