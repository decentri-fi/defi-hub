package io.defitrack

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DefitrackEventsApp

fun main(args: Array<String>) {
    runApplication<DefitrackEventsApp>(*args)
}