package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class SolidLizardApp : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.SOLIDLIZARD
    }
}

fun main(args: Array<String>) {
    runApplication<SolidLizardApp>(*args)
}