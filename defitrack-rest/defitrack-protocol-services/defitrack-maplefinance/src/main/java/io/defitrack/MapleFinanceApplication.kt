package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class MapleFinanceApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.MAPLEFINANCE
    }
}

fun main(args: Array<String>) {
    runApplication<MapleFinanceApplication>(*args)
}