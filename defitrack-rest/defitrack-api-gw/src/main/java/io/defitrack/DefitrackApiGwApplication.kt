package io.defitrack

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DefitrackApiGwApplication

fun main(args: Array<String>) {
    runApplication<DefitrackApiGwApplication>(*args)
}