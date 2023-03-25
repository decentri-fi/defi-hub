package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class RibbonAplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.RIBBON
    }
}

fun main(args: Array<String>) {
    runApplication<RibbonAplication>(*args)
}