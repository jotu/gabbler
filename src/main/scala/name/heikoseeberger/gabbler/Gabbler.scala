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

import akka.actor.{ Actor, ActorLogging, ActorRef, ReceiveTimeout }
import scala.concurrent.duration.Duration
import spray.http.StatusCodes
import spray.httpx.SprayJsonSupport
import spray.routing.RequestContext

class Gabbler(username: String, timeout: Duration) extends Actor with ActorLogging {

  import GabblerHub._
  import SprayJsonSupport._

  var messages = List.empty[Message]

  var storedRequestContext: Option[RequestContext] = None

  context.setReceiveTimeout(timeout)

  def receive = {
    case GetMessages(_, requestContext) =>
      if (!messages.isEmpty)
        completeWithMessages(requestContext)
      else {
        noContent()
        storedRequestContext = Some(requestContext)
      }
    case message: Message =>
      messages +:= message
      storedRequestContext foreach { requestContext =>
        completeWithMessages(requestContext)
        storedRequestContext = None
      }
    case ReceiveTimeout =>
      context.parent ! GabblerAskingToStop(username)
    case GabblerConfirmedToStop =>
      context.stop(self)
  }

  override def postStop() = noContent()

  def completeWithMessages(requestContext: RequestContext) = {
    log.debug("Sending {} messages to {}", messages.size, username)
    requestContext.complete(messages)
    messages = Nil
  }

  def noContent() = storedRequestContext foreach (_.complete(StatusCodes.NoContent))
}
