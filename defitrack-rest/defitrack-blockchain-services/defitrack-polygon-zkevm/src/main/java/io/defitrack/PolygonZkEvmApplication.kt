package io.defitrack

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PolygonZkEvmApplication

fun main(args: Array<String>) {
    runApplication<PolygonZkEvmApplication>(*args)
}