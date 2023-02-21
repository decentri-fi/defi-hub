package io.defitrack

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class StarknetApp

fun main(args: Array<String>) {
    runApplication<StarknetApp>(*args)
}