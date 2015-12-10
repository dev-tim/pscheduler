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

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.PropertyChecks
import org.scalatest.time.{Seconds, Millis, Span}
import org.scalatest.{FlatSpec, Matchers, OptionValues}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future, Promise}

class PSchedulerSpec extends FlatSpec with Matchers with OptionValues with ScalaFutures with PropertyChecks {

  override implicit def patienceConfig = PatienceConfig(
    timeout = scaled(Span(5, Seconds)),
    interval = scaled(Span(50, Millis))
  )

  it should "run task if wasn't run before" in {
    @volatile var ran = false
    val persistence = new MockTaskPersistence(List.empty)
    val taskName = "foo"
    val scheduler = new PScheduler(
      persistence,
      InstantScheduler,
      Duration.ofMillis(0),
      List(TaskConfiguration(taskName, Daily.atMidnight, () => Future { ran = true }))
    )

    scheduler.start()

    whenReady(persistence.saveFuture) { savedTask =>
      ran shouldBe true
      savedTask.name shouldEqual taskName
      scheduler.stop()
    }
  }

  val UTC = ZoneId.of("UTC")

  val table = Table(
    ("optionalLastRun", "now", "schedule", "shouldRun"),
    (None,                                                    LocalDateTime.of(2015, 11, 24, 10, 0).atZone(UTC), Daily.atMidnight, true),
    (Some(LocalDateTime.of(2015, 11, 23, 10, 0).atZone(UTC)), LocalDateTime.of(2015, 11, 24, 10, 0).atZone(UTC), Daily.atMidnight, true),
    (Some(LocalDateTime.of(2015, 11, 24,  9, 0).atZone(UTC)), LocalDateTime.of(2015, 11, 24, 10, 0).atZone(UTC), Daily.atMidnight, false)
  )

  it should "not run daily task if was started in the same day" in {
    forAll(table) { (optionalLastRun, givenNow, schedule, shouldRun) =>
      @volatile var ran = false
      val taskName = "foo"
      val persistence = new MockTaskPersistence(optionalLastRun.map(lastRun => Task(taskName, lastRun.toInstant)).toSeq)
      val scheduler = new PScheduler(
        persistence,
        InstantScheduler,
        Duration.ofMillis(0),
        List(TaskConfiguration(taskName, schedule, () => Future { ran = true }))
      ) {
        override protected def now: Instant = givenNow.toInstant
        override protected def zone: ZoneId = UTC
      }

      whenReady(scheduler.start()) { _ =>
        ran shouldBe shouldRun
        scheduler.stop()
      }
    }
  }
}

class MockTaskPersistence(stubbedSavedTasks: Seq[Task]) extends TasksPersistence {
  private val saved = Promise[Task]()
  override def savedTasks(implicit ec: ExecutionContext): Future[Seq[Task]] = Future.successful(stubbedSavedTasks)
  override def save(task: Task)
                   (implicit ec: ExecutionContext): Future[Unit] = saved.success(task).future.map(_ => Unit)

  def saveFuture = saved.future
}

object InstantScheduler extends InMemoryScheduler {
  @volatile var stopped = false

  override def schedule(job: => Future[Unit], interval: Duration)
                       (implicit ec: ExecutionContext): Cancellable = {
    if (!stopped) job
    new Cancellable {
      override def cancel(): Unit = {
        stopped = true
      }
    }
  }
}