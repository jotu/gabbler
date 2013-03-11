/*
 * Copyright 2013 Heiko Seeberger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package name.heikoseeberger.gabbler

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import scala.concurrent.duration.{ Duration, MILLISECONDS }
import spray.json.DefaultJsonProtocol
import spray.routing.RequestContext

object GabblerHub {

  case class GetMessages(username: String, requestContext: RequestContext)

  object InboundMessage extends DefaultJsonProtocol {
    implicit val format = jsonFormat1(apply)
  }

  case class InboundMessage(text: String)

  object Message extends DefaultJsonProtocol {
    implicit val format = jsonFormat2(apply)
  }

  case class Message(username: String, text: String)

  case class GabblerAskingToStop(username: String)

  case object GabblerConfirmedToStop
}

class GabblerHub extends Actor with ActorLogging {

  import GabblerHub._

  val gabblerTimeout =
    Duration(context.system.settings.config getMilliseconds "gabbler.timeout", MILLISECONDS)

  var gabblers = Map.empty[String, ActorRef]

  def receive = {
    case getMessages @ GetMessages(username, _) =>
      log.debug("{} has asked for messages", username)
      gabblers.getOrElse(username, createGabbler(username)) ! getMessages
    case message @ Message(username, text) =>
      log.debug("{} has sent the message '{}'", username, text)
      gabblers.values foreach (_ ! message)
    case GabblerAskingToStop(username) =>
      gabblers(username) ! GabblerConfirmedToStop
      gabblers -= username
  }

  def createGabbler(username: String) = {
    val gabbler = context.actorOf(Props(new Gabbler(username, gabblerTimeout)))
    gabblers += username -> gabbler
    gabbler
  }
}
