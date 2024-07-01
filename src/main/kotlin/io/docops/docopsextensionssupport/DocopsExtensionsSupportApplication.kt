/*
 * Copyright (c) 2023. The DocOps Consortium
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.docops.docopsextensionssupport

import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference


@SpringBootApplication
@EnableAspectJAutoProxy
class DocopsExtensionsSupportApplication
{
    companion object {
        val START  = AtomicReference<Instant>(Instant.now())
        val STOP  = AtomicReference<Instant>()
    }

    @Component
    class StartListener {
        @EventListener(ApplicationReadyEvent::class)
        fun ready() {
            STOP.set(Instant.now())
        }
    }
    @Component
    class StartupDuration : InfoContributor {
        override fun contribute(builder: Info.Builder) {
            val delta = Duration.between(START.get(), STOP.get())
            builder.withDetail("startupDuration", "${delta.toMillis()} ms")
        }

    }
}

fun main(args: Array<String>) {
    runApplication<DocopsExtensionsSupportApplication>(*args)
}
