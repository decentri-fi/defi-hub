package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class TornadocashApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.TORNADO_CASH
    }
}

fun main(args: Array<String>) {
    runApplication<TornadocashApplication>(*args)
}