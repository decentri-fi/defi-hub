package io.defitrack

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DefitrackTransactionsApp

fun main(args: Array<String>) {
    runApplication<DefitrackTransactionsApp>(*args)
}