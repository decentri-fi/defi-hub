package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class DodoApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.DODO
    }
}

fun main(args: Array<String>) {
    runApplication<DodoApplication>(*args)
}