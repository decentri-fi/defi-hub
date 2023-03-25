package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class MakerDAOApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.MAKERDAO
    }
}

fun main(args: Array<String>) {
    runApplication<MakerDAOApplication>(*args)
}