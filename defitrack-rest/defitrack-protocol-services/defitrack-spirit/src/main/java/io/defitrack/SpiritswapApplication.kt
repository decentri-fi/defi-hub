package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableAsync

class SpiritswapApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.SPIRITSWAP
    }
}

fun main(args: Array<String>) {
    runApplication<SpiritswapApplication>(*args)
}