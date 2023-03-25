package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling


class BeefyApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.BEEFY
    }
}

fun main(args: Array<String>) {
    runApplication<BeefyApplication>(*args)
}
