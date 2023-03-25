package io.defitrack

import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class IronBankApplication : ProtocolApplication() {
    override fun getProtocol(): Protocol {
        return Protocol.IRON_BANK
    }
}

fun main(args: Array<String>) {
    runApplication<IronBankApplication>(*args)
}