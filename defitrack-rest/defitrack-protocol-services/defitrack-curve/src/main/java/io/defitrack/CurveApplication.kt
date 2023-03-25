package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class CurveApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.CURVE
    }
}

fun main(args: Array<String>) {
    runApplication<CurveApplication>(*args)
}