package io.defitrack

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DefitrackPolygonApp

fun main(args: Array<String>) {
    runApplication<DefitrackPolygonApp>(*args)
}