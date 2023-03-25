package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class ConvexApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.CONVEX
    }
}

fun main(args: Array<String>) {
    runApplication<ConvexApplication>(*args)
}