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

import pl.touk.pscheduler.slick.migration.CreatingTasksTableMigration
import pl.touk.pscheduler.{Task, TasksPersistence}
import slick.driver.JdbcDriver
import slick.jdbc.JdbcBackend

import scala.concurrent.{ExecutionContext, Future}

class SlickJdbcTasksPersistence(driver: JdbcDriver, db: JdbcBackend.Database) extends TasksPersistence {

  val tasksMigration = new CreatingTasksTableMigration {
    override protected val driver: JdbcDriver = SlickJdbcTasksPersistence.this.driver
  }

  import tasksMigration._
  import driver.api._

  override def savedTasks(implicit ec: ExecutionContext): Future[Seq[Task]] = {
    db.run(tasks.result)
  }

  override def save(task: Task)(implicit ec: ExecutionContext): Future[Unit] = {
    db.run(tasks.insertOrUpdate(task)).map(_ => Unit)
  }

}