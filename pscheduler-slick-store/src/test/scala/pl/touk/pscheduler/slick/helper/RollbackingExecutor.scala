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
package pl.touk.pscheduler.slick.helper

import java.sql.Connection

import com.typesafe.slick.testkit.util.DelegateConnection
import slick.driver.JdbcDriver
import slick.jdbc.{JdbcBackend, JdbcDataSource}

import scala.concurrent.Await

class RollbackingExecutor(driver: JdbcDriver, db: JdbcBackend.Database) {
  import driver.api._

  def run[T](f: JdbcBackend.Database => T): T = {
    val session = db.createSession()
    try {
      session.withTransaction {
        val singleSessionDb = createSingleSessionDatabase(session)
        val result = f(singleSessionDb)
        result
      }
    } finally {
      try {
        session.conn.prepareCall("SHUTDOWN").execute()
      } finally {
        session.close()
      }
    }
  }

  private def createSingleSessionDatabase(session: Session): JdbcBackend.Database = {
    val wrappedConn = new DelegateConnection(session.conn) {
      override def setAutoCommit(autoCommit: Boolean): Unit = ()
      override def commit(): Unit = ()
      override def close(): Unit = ()
    }
    JdbcBackend.Database.forSource(new JdbcDataSource {
      def createConnection(): Connection = wrappedConn
      def close(): Unit = ()
    })
  }
}