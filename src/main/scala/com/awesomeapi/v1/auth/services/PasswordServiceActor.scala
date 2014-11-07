package com.awesomeapi.v1.auth.services

import akka.actor.Actor
import com.awesomeapi.v1.auth.{PasswordData}
import com.awesomeapi.libs.{MailerFormat, RabbitMQConnection}
import com.awesomeapi.domain.Users
import concurrent.ExecutionContext.Implicits.global

class PasswordServiceActor extends Actor{


  def receive: Receive = {
    case reqData: PasswordData => {
      Users.findByEmail(reqData.email) match {
        case Some(u) => {
          MailerFormat.create('recovery, Array(("to", u.email), ("subject", "prueba"))) onSuccess {
            case x => println("CD -> " + x)
          }
        }
        case None => sender ! None
      }
    }
  }
}
//val connection = RabbitMQConnection.getConnection()
//val channel = connection.createChannel()
//channel.queueDeclare("queue1", false, false, false, null);
//val msg = "prueba"
//channel.basicPublish("", "queue1", null, msg.getBytes())

