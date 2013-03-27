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

import akka.actor.{ Actor, Cancellable }
import scala.concurrent.duration.FiniteDuration

object Gabbler {
  case object Timeout
}

class Gabbler(username: String, timeout: FiniteDuration) extends Actor {

  import Gabbler._
  import GabblerHub._
  import context.dispatcher

  def receive: Receive =
    idling(context.system.scheduler.scheduleOnce(timeout, self, Timeout))

  def idling(timeoutTask: Cancellable): Receive = {
    case GetMessages(_, completer) =>
      context become waitingForMessage(completer, newTimeout(timeoutTask))
    case message: Message =>
      context become collectingMessages(message :: Nil, timeoutTask)
    case Timeout =>
      context.stop(self)
  }

  def waitingForMessage(completer: Completer, timeoutTask: Cancellable): Receive = {
    case GetMessages(_, newCompleter) =>
      context become waitingForMessage(newCompleter, newTimeout(timeoutTask))
    case message: Message =>
      completer(message :: Nil)
      context become idling(timeoutTask)
    case Timeout =>
      completer(Nil)
      context become idling(newTimeout(timeoutTask))
  }

  def collectingMessages(messages: List[Message], timeoutTask: Cancellable): Receive = {
    case GetMessages(_, completer) =>
      completer(messages)
      context become idling(newTimeout(timeoutTask))
    case message: Message =>
      context become collectingMessages(message :: messages, timeoutTask)
    case Timeout =>
      context.stop(self)
  }

  def newTimeout(timeoutTask: Cancellable): Cancellable = {
    timeoutTask.cancel()
    context.system.scheduler.scheduleOnce(timeout, self, Timeout)
  }
}
