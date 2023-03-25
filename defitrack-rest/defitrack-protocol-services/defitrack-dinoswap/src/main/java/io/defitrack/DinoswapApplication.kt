package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class DinoswapApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.DINOSWAP
    }
}

fun main(args: Array<String>) {
    runApplication<DinoswapApplication>(*args)
}