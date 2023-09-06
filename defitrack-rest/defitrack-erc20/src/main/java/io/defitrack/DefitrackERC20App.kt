package io.defitrack

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DefitrackERC20App

fun main(args: Array<String>) {
    runApplication<DefitrackERC20App>(*args)
}