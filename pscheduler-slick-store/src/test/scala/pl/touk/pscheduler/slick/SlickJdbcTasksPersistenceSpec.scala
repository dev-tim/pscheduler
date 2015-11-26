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
package pl.touk.pscheduler.slick

import java.time.{LocalDateTime, ZoneId}

import org.scalatest.Matchers
import org.scalatest.concurrent.ScalaFutures
import pl.touk.pscheduler.Task
import pl.touk.pscheduler.slick.helper.SlickJdbcSpec
import slick.jdbc.JdbcBackend

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

class SlickJdbcTasksPersistenceSpec extends SlickJdbcSpec with Matchers with ScalaFutures {

  it should "initially return empty tasks list" in { persistence =>
    whenReady(persistence.savedTasks)(_ shouldBe empty)
  }

  val UTC = ZoneId.of("UTC")

  it should "return saved task after save" in { persistence =>
    val task = Task("foo", LocalDateTime.of(2015, 11, 24, 10, 0).atZone(UTC).toInstant)
    val afterSaveF = for {
      _ <- persistence.save(task)
      afterSave <- persistence.savedTasks
    } yield afterSave
    whenReady(afterSaveF) { afterSaveTasks =>
      afterSaveTasks should have size 1
      afterSaveTasks.head shouldEqual task
    }
  }

  override protected def createFixture(db: JdbcBackend.Database) = {
    new SlickJdbcTasksPersistence(driver, db)
  }

  override type FixtureParam = SlickJdbcTasksPersistence
}