package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class QuickswapApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.QUICKSWAP
    }
}

fun main(args: Array<String>) {
    runApplication<QuickswapApplication>(*args)
}