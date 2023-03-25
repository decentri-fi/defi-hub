package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class LooksRareApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.LOOKSRARE
    }
}

fun main(args: Array<String>) {
    runApplication<LooksRareApplication>(*args)
}