package io.defitrack

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DefitrackBscApp

fun main(args: Array<String>) {
    runApplication<DefitrackBscApp>(*args)
}