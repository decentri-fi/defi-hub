package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class MStableApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.MSTABLE
    }
}

fun main(args: Array<String>) {
    runApplication<MStableApplication>(*args)
}