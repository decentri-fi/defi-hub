package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class SpookyApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.SPOOKY
    }
}

fun main(args: Array<String>) {
    runApplication<SpookyApplication>(*args)
}