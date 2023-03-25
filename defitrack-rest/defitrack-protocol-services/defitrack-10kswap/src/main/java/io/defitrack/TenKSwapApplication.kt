package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class TenKSwapApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        throw NotImplementedError("Not implemented")
    }
}

fun main(args: Array<String>) {
    runApplication<TenKSwapApplication>(*args)
}