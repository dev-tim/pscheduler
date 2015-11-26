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

import java.time._

import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

class PScheduler(persistence: TasksPersistence,
                 checkScheduler: InMemoryScheduler,
                 checkInterval: Duration,
                 configuration: Seq[TaskConfiguration]) {

  protected def now: Instant = Instant.now()

  protected def zone: ZoneId = ZoneId.systemDefault()

  private val logger = LoggerFactory.getLogger(getClass)

  @volatile var scheduledCheck: Option[Cancellable] = None

  def start()(implicit ec: ExecutionContext): Future[Unit] = {
    runScheduledTasks()
  }

  private def runScheduledTasks()(implicit ec: ExecutionContext): Future[Unit] = {
    runScheduledTasks(now)
  }

  private def runScheduledTasks(now: Instant)
                               (implicit ec: ExecutionContext): Future[Unit] = {
    val tasksRunF = for {
      saved <- persistence.savedTasks
      _ = {
        logger.debug(saved.mkString("Fetched tasks:\n", "\n", "\nChecking if some of them should be run"))
      }
      savedByName = saved.map(task => task.name -> task).toMap
      shouldRun = shouldRunNow(savedByName, now) _
      tasksToRun = configuration.filter(shouldRun)
      tasksResult <- Future.sequence(tasksToRun.map { task =>
        runTaskThanUpdateLastRun(now, task)
      })
    } yield ()
    tasksRunF.onComplete { _ =>
      scheduledCheck = Some(checkScheduler.schedule(runScheduledTasks(), checkInterval))
    }
    tasksRunF
  }

  private def shouldRunNow(savedByName: Map[String, Task], now: Instant)
                          (config: TaskConfiguration): Boolean = {
    val optionalLastRun = savedByName.get(config.taskName).map(_.lastRun)
    config.schedule.shouldRun(
      optionalLastRun.map(LocalDateTime.ofInstant(_, zone)),
      LocalDateTime.ofInstant(now, zone)
    )
  }

  private def runTaskThanUpdateLastRun(now: Instant, task: TaskConfiguration)
                                      (implicit ec: ExecutionContext): Future[Unit] = {
    logger.debug(s"Running task: ${task.taskName}")
    for {
      _ <- task.run()
      _ <- persistence.save(Task(task.taskName, now))
    } yield ()
  }

  def stop(): Unit = {
    scheduledCheck.foreach(_.cancel())
  }
}

object PScheduler {
  def builder = PSchedulerBuilder.withCheckInterval(Duration.ofMinutes(5))
}

case class TaskConfiguration(taskName: String, schedule: TaskSchedule, run: () => Future[Unit])