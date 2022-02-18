package io.defitrack

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HumanDAOApplication

fun main(args: Array<String>) {
    runApplication<HumanDAOApplication>(*args)
}