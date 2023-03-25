package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class BeethovenXApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.BEETHOVENX
    }
}

fun main(args: Array<String>) {
    runApplication<BeethovenXApplication>(*args)
}