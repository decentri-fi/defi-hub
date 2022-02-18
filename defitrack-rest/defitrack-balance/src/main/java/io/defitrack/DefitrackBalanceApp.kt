package io.defitrack

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DefitrackBalanceApp

fun main(args: Array<String>) {
    runApplication<DefitrackBalanceApp>(*args)
}