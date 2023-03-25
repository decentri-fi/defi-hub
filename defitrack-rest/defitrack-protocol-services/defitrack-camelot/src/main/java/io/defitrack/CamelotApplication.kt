package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class CamelotApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.CAMELOT
    }
}

fun main(args: Array<String>) {
    runApplication<CamelotApplication>(*args)
}