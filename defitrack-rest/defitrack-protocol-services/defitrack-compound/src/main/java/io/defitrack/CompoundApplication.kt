package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class CompoundApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.COMPOUND
    }
}

fun main(args: Array<String>) {
    runApplication<CompoundApplication>(*args)
}