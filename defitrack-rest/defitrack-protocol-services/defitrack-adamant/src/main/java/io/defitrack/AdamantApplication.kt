package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class AdamantApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.ADAMANT
    }
}

fun main(args: Array<String>) {
    runApplication<AdamantApplication>(*args)
}