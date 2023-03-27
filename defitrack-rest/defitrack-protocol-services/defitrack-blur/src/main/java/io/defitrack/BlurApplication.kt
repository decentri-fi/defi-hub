package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class BlurApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.BLUR
    }
}

fun main(args: Array<String>) {
    runApplication<BlurApplication>(*args)
}