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

import scala.concurrent.duration.{ Duration, FiniteDuration }
import akka.actor.{ ReceiveTimeout, Actor, ActorLogging }

class Gabbler(username: String, timeout: FiniteDuration) extends Actor with ActorLogging {
  import GabblerHub._

  def receive = idling

  def idling: Receive = {
    case GetMessages(_, completer) =>
      context setReceiveTimeout timeout
      context become waitingForMessage(completer)

    case message: Message =>
      context setReceiveTimeout Duration.Undefined
      context become collectingMessages(message :: Nil)

    case ReceiveTimeout =>
      log.debug("Timeout, shutting down")
      context stop self
  }

  def collectingMessages(messages: List[Message]): Receive = {
    case GetMessages(_, completer) =>
      log.debug("Sending {} messages to {}", messages.size, username)
      completer(messages)
      context setReceiveTimeout timeout
      context become idling

    case message: Message => context become collectingMessages(message :: messages)
  }

  def waitingForMessage(completer: List[Message] => Unit): Receive = {
    case GetMessages(_, newCompleter) =>
      context become waitingForMessage(newCompleter)

    case message: Message =>
      log.debug("Sending message to {}", username)
      completer(message :: Nil)
      context become idling

    case ReceiveTimeout =>
      log.debug("Timeout, sending empty message list to {}", username)
      completer(Nil)
      context become idling
  }

}
