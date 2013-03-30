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

import akka.actor.{ ExtendedActorSystem, Extension, ExtensionId }
import com.typesafe.config.Config
import scala.concurrent.duration.{ Duration, FiniteDuration, MILLISECONDS }

object GabblerSettings extends ExtensionId[GabblerSettings] {

  def createExtension(system: ExtendedActorSystem): GabblerSettings =
    new GabblerSettings(system.settings.config)
}

class GabblerSettings(config: Config) extends Extension {

  val timeout: FiniteDuration =
    Duration(config getMilliseconds "gabbler.timeout", MILLISECONDS)
}
