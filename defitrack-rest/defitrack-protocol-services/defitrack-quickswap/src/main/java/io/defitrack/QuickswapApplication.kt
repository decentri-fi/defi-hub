package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class QuickswapApplication : ProtocolApplication() {

    override fun getCompany(): Company {
        return Company.QUICKSWAP
    }
}

fun main(args: Array<String>) {
    runApplication<QuickswapApplication>(*args)
}