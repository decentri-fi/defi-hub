package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class DinoswapApplication : ProtocolApplication() {
    override fun getCompany(): Company {
        return Company.DINOSWAP
    }
}

fun main(args: Array<String>) {
    runApplication<DinoswapApplication>(*args)
}