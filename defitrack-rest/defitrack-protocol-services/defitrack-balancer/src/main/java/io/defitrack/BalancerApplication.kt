package io.defitrack

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BalancerApplication

fun main(args: Array<String>) {
    runApplication<BalancerApplication>(*args)
}