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

import akka.actor.{ Actor, ActorLogging, ReceiveTimeout }
import scala.concurrent.duration.Duration

class Gabbler(username: String, timeout: Duration) extends Actor with ActorLogging {

  import GabblerHub._

  var messages: List[Message] =
    Nil

  var storedCompleter: Option[List[Message] => Unit] =
    None

  context.setReceiveTimeout(timeout)

  override def receive: Receive = {
    case GetMessages(_, completer) =>
      if (messages.isEmpty) {
        noContent()
        storedCompleter = Some(completer)
      } else
        drainMessages(completer)
    case message: Message =>
      messages +:= message
      storedCompleter foreach drainMessages
    case ReceiveTimeout =>
      context.parent ! GabblerAskingToStop(username)
    case GabblerConfirmedToStop =>
      context.stop(self)
  }

  override def postStop(): Unit =
    noContent()

  def drainMessages(completer: List[Message] => Unit): Unit = {
    log.debug("Sending {} messages to {}", messages.size, username)
    completer(messages)
    storedCompleter = None
    messages = Nil
  }

  def noContent(): Unit =
    storedCompleter foreach (_(Nil))
}
