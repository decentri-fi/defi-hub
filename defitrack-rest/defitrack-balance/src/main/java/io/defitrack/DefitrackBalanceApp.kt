package io.defitrack

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
class DefitrackBalanceApp

fun main(args: Array<String>) {
    runApplication<DefitrackBalanceApp>(*args)
}