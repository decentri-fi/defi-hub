package io.defitrack

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.availability.AvailabilityChangeEvent
import org.springframework.boot.availability.ReadinessState
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext

@SpringBootApplication
class DefitrackERC20App

fun main(args: Array<String>) {
    runApplication<DefitrackERC20App>(*args)
}