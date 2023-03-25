package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class ApeswapAplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.APESWAP
    }
}

fun main(args: Array<String>) {
    runApplication<ApeswapAplication>(*args)
}