package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class SwapfishApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.SWAPFISH
    }
}

fun main(args: Array<String>) {
    runApplication<SwapfishApplication>(*args)
}