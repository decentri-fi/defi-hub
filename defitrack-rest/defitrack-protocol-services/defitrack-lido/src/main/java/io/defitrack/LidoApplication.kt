package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class LidoApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.LIDO
    }
}

fun main(args: Array<String>) {
    runApplication<LidoApplication>(*args)
}