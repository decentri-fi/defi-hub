package io.defitrack

import io.defitrack.protocol.Company
import org.springframework.boot.runApplication

class SpiritswapApplication : ProtocolApplication() {

    override fun getCompany(): Company {
        return Company.SPIRITSWAP
    }
}

fun main(args: Array<String>) {
    runApplication<SpiritswapApplication>(*args)
}