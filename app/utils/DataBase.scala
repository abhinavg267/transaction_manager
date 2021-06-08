package utils

import service.models.Transactions
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcProfile

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object DataBase {
  val h2DataBase = DatabaseConfig.forConfig[JdbcProfile]("h2_dc").db

  // create tables
  Await.result(h2DataBase.run((Transactions.query.schema).create), Duration.Inf)
}
