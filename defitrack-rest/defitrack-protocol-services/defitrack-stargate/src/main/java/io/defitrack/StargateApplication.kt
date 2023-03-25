package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class StargateApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.STARGATE
    }
}

fun main(args: Array<String>) {
    runApplication<StargateApplication>(*args)
}