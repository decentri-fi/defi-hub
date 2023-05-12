package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication


class CowswapApplication : ProtocolApplication() {
    override fun getCompany(): Company {
        return Company.COWSWAP
    }
}

fun main(args: Array<String>) {
    runApplication<CowswapApplication>(*args)
}