package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

class HopApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.HOP
    }
}

fun main(args: Array<String>) {
    runApplication<HopApplication>(*args)
}