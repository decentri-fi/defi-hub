package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class PolycatApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.POLYCAT
    }
}

fun main(args: Array<String>) {
    runApplication<PolycatApplication>(*args)
}