package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class AuraApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.AURA
    }
}

fun main(args: Array<String>) {
    runApplication<AuraApplication>(*args)
}