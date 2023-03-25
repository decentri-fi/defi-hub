package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

class VelodromeApplication: ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.VELODROME
    }
}

fun main(args: Array<String>) {
    runApplication<VelodromeApplication>(*args)
}