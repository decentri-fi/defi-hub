package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class UniswapApplication : ProtocolApplication() {
    override fun getCompany(): Company {
        return Company.UNISWAP
    }
}

fun main(args: Array<String>) {
    runApplication<UniswapApplication>(*args)
}