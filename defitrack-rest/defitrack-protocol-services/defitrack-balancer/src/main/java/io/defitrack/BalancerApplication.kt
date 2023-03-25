package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class BalancerApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.BALANCER
    }
}

fun main(args: Array<String>) {
    runApplication<BalancerApplication>(*args)
}