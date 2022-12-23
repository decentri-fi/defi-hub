package io.defitrack

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SetApp

fun main(args: Array<String>) {
    runApplication<SetApp>(*args)
}