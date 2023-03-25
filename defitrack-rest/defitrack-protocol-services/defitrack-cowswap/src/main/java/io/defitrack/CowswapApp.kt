package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication


class CowswapApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.COWSWAP
    }
}

fun main(args: Array<String>) {
    runApplication<CowswapApplication>(*args)
}