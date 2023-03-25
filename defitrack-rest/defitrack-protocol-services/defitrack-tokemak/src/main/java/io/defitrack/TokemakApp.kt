package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class TokemakApp : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.TOKEMAK
    }
}

fun main(args: Array<String>) {
    runApplication<TokemakApp>(*args)
}