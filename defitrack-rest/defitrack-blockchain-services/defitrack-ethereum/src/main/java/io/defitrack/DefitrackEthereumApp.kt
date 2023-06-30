package io.defitrack

import io.micrometer.core.aop.TimedAspect
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class DefitrackEthereumApp

fun main(args: Array<String>) {
    runApplication<DefitrackEthereumApp>(*args)
}