package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class UniswapApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.UNISWAP
    }
}

fun main(args: Array<String>) {
    runApplication<UniswapApplication>(*args)
}