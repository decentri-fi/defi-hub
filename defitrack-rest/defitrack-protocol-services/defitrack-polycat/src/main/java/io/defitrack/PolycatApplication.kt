package io.defitrack

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PolycatApplication

fun main(args: Array<String>) {
    runApplication<PolycatApplication>(*args)
}