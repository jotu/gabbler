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

import akka.actor.{ Actor, ActorLogging, Cancellable }
import scala.concurrent.duration.FiniteDuration

object Gabbler {
  case object Timeout
}

class Gabbler(username: String, timeout: FiniteDuration) extends Actor with ActorLogging {

  import Gabbler._
  import GabblerHub._

  var messages: List[Message] = Nil

  var storedCompleter: Option[List[Message] => Unit] = None

  var aboutToStop: Boolean = false

  var cancellable: Cancellable = scheduleTimeout()

  override def receive: Receive = {
    case GetMessages(_, completer) =>
      aboutToStop = false
      scheduleNewTimeout()
      if (messages.isEmpty)
        storedCompleter = Some(completer)
      else
        completeWithMessages(completer)
    case message: Message =>
      messages +:= message
      storedCompleter foreach completeWithMessages
      storedCompleter = None
    case Timeout =>
      if (aboutToStop)
        context.stop(self)
      else {
        aboutToStop = true
        scheduleNewTimeout()
        completeEmpty()
        storedCompleter = None
      }
  }

  def completeEmpty(): Unit =
    storedCompleter foreach (_(Nil))

  def completeWithMessages(completer: List[Message] => Unit): Unit = {
    log.debug("Sending {} messages to {}", messages.size, username)
    completer(messages)
    messages = Nil
  }

  def scheduleNewTimeout(): Unit = {
    cancellable.cancel()
    cancellable = scheduleTimeout
  }

  def scheduleTimeout(): Cancellable =
    context.system.scheduler.scheduleOnce(timeout, self, Timeout)(context.dispatcher)
}
