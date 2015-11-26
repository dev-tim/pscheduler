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
package pl.touk.pscheduler.slick.migration

import java.sql.Timestamp

import pl.touk.pscheduler.Task
import slick.profile.SqlProfile.ColumnOption.NotNull

import scala.language.postfixOps

trait CreatingTasksTableMigration extends SlickJdbcMigration {

  import driver.api._

  override def migrateActions = {
    tasks.schema.create
  }

  val tasks = TableQuery[TaskEntity]

  class TaskEntity(tag: Tag) extends Table[Task](tag, "tasks") {

    def name = column[String]("name", NotNull, O.PrimaryKey, O.Length(32))
    def lastRun = column[Timestamp]("last_run", NotNull)

    def * = (name, lastRun) <> (createTask _ tupled, extractTask)
  }

  private def createTask(name: String, lastRun: Timestamp): Task = {
    Task(name, lastRun.toInstant)
  }

  private val extractTask = Task.unapply _ andThen { opt =>
    opt.map {
      case (name, instant) =>
        (name, new Timestamp(instant.toEpochMilli))
    }
  }

}