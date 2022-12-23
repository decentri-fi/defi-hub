package io.defitrack

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class AdamantApplication

fun main(args: Array<String>) {
    runApplication<AdamantApplication>(*args)
}