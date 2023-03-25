package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class DfynApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.DFYN
    }
}

fun main(args: Array<String>) {
    runApplication<DfynApplication>(*args)
}