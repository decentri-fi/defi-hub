package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class OlympusDAOApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.OLYMPUSDAO
    }
}

fun main(args: Array<String>) {
    runApplication<OlympusDAOApplication>(*args)
}