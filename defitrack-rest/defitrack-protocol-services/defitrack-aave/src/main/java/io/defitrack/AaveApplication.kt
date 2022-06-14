package io.defitrack

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AaveApplication

fun main(args: Array<String>) {
    runApplication<AaveApplication>(*args)
}