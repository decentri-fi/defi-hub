package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class QidaoApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.QIDAO
    }
}

fun main(args: Array<String>) {
    runApplication<QidaoApplication>(*args)
}