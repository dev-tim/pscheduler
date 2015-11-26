# pscheduler

[![Build Status](https://travis-ci.org/TouK/pscheduler.svg)](https://travis-ci.org/TouK/pscheduler)

*pscheduler* is a simple, lightweight, persistent scheduler. It's abstracting from in-memory scheduler which will be used for check that some task should be run and from persistence backend.
Currently there are implemented *Akka* in memory scheduler and *Slick* jdbc persistence in separate modules.

## QuickStart

In your sbt:

```sbt
libraryDependencies += "pl.touk.pscheduler" %% "pscheduler-akka" % "0.1.0"
libraryDependencies += "pl.touk.pscheduler" %% "pscheduler-slick-store" % "0.1.0"
```

And then start scheduler:

```scala
import pl.touk.pscheduler._
import pl.touk.pscheduler.akka._
import pl.touk.pscheduler.slick._

val pScheduler =
  PScheduler.builder
    .withAkkaScheduler(scheduler)
    .withSlickJdbcPersistence(PostgresDriver, db)
    .withTasks(
      TaskConfiguration(
        "limits_reset",
        Daily.atMidnight,
        () => limitsService.reset())
    ).build

pScheduler.start()
```

## Overview

After *start* invocation it periodically schedules verification if some task run is needed. For this, it fetches last run tasks.
Then, using configuration, scheduler checks if task should be run again. After successful run, it saves last run date.

## Database migration

*pscheduler-slick-store* module provides *Flyway* migration script. To use it just create class:
 
 ```scala
 package db.migration
 
 import pl.touk.pscheduler.slick.migration.CreatingTasksTableMigration
 import slick.driver.{HsqldbDriver, JdbcDriver}
 
 class V1_001__CreateTasksTable extends CreatingTasksTableMigration {
   override protected val driver: JdbcDriver = HsqldbDriver
 }
 ```

For more info take a look at tests.

## License

The pscheduler is released under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).