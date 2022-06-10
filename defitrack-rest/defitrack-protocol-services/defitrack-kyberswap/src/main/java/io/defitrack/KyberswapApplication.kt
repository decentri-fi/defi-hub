package io.defitrack

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KyberswapApplication

fun main(args: Array<String>) {
    runApplication<KyberswapApplication>(*args)
}