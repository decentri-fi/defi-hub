package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class BancorApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.BANCOR
    }
}

fun main(args: Array<String>) {
    runApplication<BancorApplication>(*args)
}