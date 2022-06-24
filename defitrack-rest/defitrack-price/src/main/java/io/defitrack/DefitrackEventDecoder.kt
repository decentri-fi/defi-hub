package io.defitrack

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DefitrackPriceApp

fun main(args: Array<String>) {
    runApplication<DefitrackPriceApp>(*args)
}