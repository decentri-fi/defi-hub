package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class WepiggyApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.WEPIGGY
    }
}

fun main(args: Array<String>) {
    runApplication<WepiggyApplication>(*args)
}