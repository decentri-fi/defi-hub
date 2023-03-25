package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class AaveApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.AAVE
    }
}

fun main(args: Array<String>) {
    runApplication<AaveApplication>(*args)
}