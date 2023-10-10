package io.defitrack

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EvmApplication

fun main(args: Array<String>) {
    runApplication<EvmApplication>(*args)
}