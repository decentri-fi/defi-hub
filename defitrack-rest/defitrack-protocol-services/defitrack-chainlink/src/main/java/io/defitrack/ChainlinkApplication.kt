package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class ChainlinkApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.CHAINLINK
    }
}

fun main(args: Array<String>) {
    runApplication<ChainlinkApplication>(*args)
}