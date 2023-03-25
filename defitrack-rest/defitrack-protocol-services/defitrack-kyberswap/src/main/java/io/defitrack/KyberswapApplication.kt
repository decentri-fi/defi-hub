package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class KyberswapApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.KYBER_SWAP
    }
}

fun main(args: Array<String>) {
    runApplication<KyberswapApplication>(*args)
}