package com.awesomeapi.libs

import scala.slick.driver.MySQLDriver.simple._
import com.jolbox.bonecp._


package object databaseAccess{

  import com.typesafe.config.ConfigFactory

  private lazy val conf = ConfigFactory.load("database")
  private lazy val url = conf.getString(Environment.env + ".url")
  private lazy val port = conf.getString(Environment.env + ".port")
  private lazy val database = conf.getString(Environment.env + ".database")
  private lazy val driver = conf.getString(Environment.env + ".driver")
  private lazy val user = conf.getString(Environment.env + ".user")
  private lazy val pass = conf.getString(Environment.env + ".password")
  private lazy val encoding = conf.getString(Environment.env + ".encoding")
  private lazy val minConnections = conf.getInt(Environment.env + ".min_connections")
  private lazy val maxConnections = conf.getInt(Environment.env + ".max_connections")
  private lazy val partitionCount = conf.getInt(Environment.env + ".partition_count")

  lazy val db = {
    val ds = new BoneCPDataSource()
    ds.setJdbcUrl(url + ":" + port + "/" + database + "?characterEncoding=" + encoding)
    ds.setDriverClass(driver)
    ds.setUsername(user)
    ds.setPassword(pass)
    ds.setMinConnectionsPerPartition(minConnections)
    ds.setMaxConnectionsPerPartition(maxConnections)
    ds.setPartitionCount(partitionCount)
    Database.forDataSource(ds)
  }


}
