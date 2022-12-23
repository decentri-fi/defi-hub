package io.defitrack

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DefitrackStatisticsApp

fun main(args: Array<String>) {
    runApplication<DefitrackStatisticsApp>(*args)
}