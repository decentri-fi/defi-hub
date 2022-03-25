package io.defitrack

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DefitrackEthereumApp

fun main(args: Array<String>) {
    runApplication<DefitrackEthereumApp>(*args)
}