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

import akka.actor.{ Actor, ActorRef, Props }
import scala.concurrent.duration.{ Duration, FiniteDuration, MILLISECONDS }
import spray.json.DefaultJsonProtocol

object GabblerHub {

  type Completer = List[Message] => Unit

  case class GetMessages(username: String, completer: Completer)

  object Message extends DefaultJsonProtocol {
    implicit val format = jsonFormat2(apply)
  }

  case class Message(username: String, text: String)
}

class GabblerHub extends Actor {

  import GabblerHub._

  val timeout: FiniteDuration =
    Duration(context.system.settings.config getMilliseconds "gabbler.timeout", MILLISECONDS)

  override def receive: Receive = {
    case getMessages @ GetMessages(username, _) =>
      gabblerFor(username) ! getMessages
    case message: Message =>
      context.children foreach (_ ! message)
  }

  def gabblerFor(username: String): ActorRef =
    context.child(username) getOrElse context.actorOf(Props(new Gabbler(username, timeout)), username)
}
