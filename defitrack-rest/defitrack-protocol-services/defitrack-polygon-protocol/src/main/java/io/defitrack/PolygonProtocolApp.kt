package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class PolygonProtocolApp : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.POLYGON
    }
}

fun main(args: Array<String>) {
    runApplication<PolygonProtocolApp>(*args)
}