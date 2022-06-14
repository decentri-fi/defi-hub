package io.defitrack

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BancorApplication

fun main(args: Array<String>) {
    runApplication<BancorApplication>(*args)
}