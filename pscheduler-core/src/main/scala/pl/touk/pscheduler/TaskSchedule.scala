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

import java.time.{LocalDateTime, LocalTime}

trait TaskSchedule {
  def shouldRun(lastRun: Option[LocalDateTime], now: LocalDateTime): Boolean
}

trait RunningImmediatelyOutstandingExecutions { self: TaskSchedule =>
  override def shouldRun(lastRun: Option[LocalDateTime], now: LocalDateTime): Boolean = {
    lastRun match {
      case Some(run) if shouldRun(run, now) => true
      case None => true
      case _ => false
    }
  }

  def shouldRun(lastRun: LocalDateTime, now: LocalDateTime): Boolean
}


object Daily {
  def at(time: LocalTime) = EveryNDays(days = 1, time)

  def atMidnight = EveryNDays(days = 1)
}

case class EveryNDays(days: Int, time: LocalTime = LocalTime.ofNanoOfDay(0))
  extends TaskSchedule with RunningImmediatelyOutstandingExecutions {

  override def shouldRun(lastRun: LocalDateTime, now: LocalDateTime): Boolean = {
    lastRun.plusDays(days).`with`(time).isBefore(now)
  }
}