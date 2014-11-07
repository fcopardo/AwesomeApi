package com.awesomeapi.libs

import com.rabbitmq.client.{Connection, ConnectionFactory}

object RabbitMQConnection {
  import com.typesafe.config.ConfigFactory

  lazy val conf = ConfigFactory.load("rabbitmq.conf")
  lazy val host = conf.getString("rabbitMQ.host")
  lazy val vhost = conf.getString("rabbitMQ.vhost")
  lazy val port = conf.getInt("rabbitMQ.port")
  lazy val user = conf.getString("rabbitMQ.user")
  lazy val password = conf.getString("rabbitMQ.password")

  private var connection: Connection = null;

  def getConnection(): Connection = {
    connection match {
      case null => {
        val factory = new ConnectionFactory()
        factory.setHost(host)
        factory.setUsername(user)
        factory.setPassword(password)
        factory.setVirtualHost(vhost)
        factory.setPort(port)
        connection = factory.newConnection()
        connection
      }
      case _ => connection
    }
  }
}
