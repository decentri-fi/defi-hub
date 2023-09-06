package io.defitrack

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DefitrackENSApp

fun main(args: Array<String>) {
    runApplication<DefitrackENSApp>(*args)
}