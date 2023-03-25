package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class AelinApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.AELIN
    }
}

fun main(args: Array<String>) {
    runApplication<AelinApplication>(*args)
}