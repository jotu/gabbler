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

import akka.actor.{ Actor, ActorRef }
import spray.http.StatusCodes
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import spray.routing.HttpServiceActor
import spray.routing.authentication.BasicAuth

object GabblerService {

  object InboundMessage extends DefaultJsonProtocol {
    implicit val format = jsonFormat1(apply)
  }

  case class InboundMessage(text: String)
}

class GabblerService(gabblerHub: ActorRef) extends Actor with HttpServiceActor {

  import GabblerHub._
  import GabblerService._
  import SprayJsonSupport._

  override def receive: Receive =
    runRoute(
      // format: OFF
      authenticate(BasicAuth(UsernameEqualsPasswordAuthenticator, "gabbler"))(user =>
        path("")(
          getFromResource(s"web/index.html")
        ) ~
        pathPrefix("api" / "messages")(
          get(
            produce(instanceOf[List[Message]])(completer => _ =>
              gabblerHub ! GetMessages(user.username, completer)
            )
          ) ~
          post(
            entity(as[InboundMessage]) { message =>
              gabblerHub ! Message(user.username, message.text)
              complete(StatusCodes.NoContent)
            }
          )
        ) ~
        getFromResourceDirectory("web")
      )
    // format: ON
    )
}
