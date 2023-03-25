package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class SetApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.SET
    }
}

fun main(args: Array<String>) {
    runApplication<SetApplication>(*args)
}