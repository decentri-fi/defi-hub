package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class BaseSwapApplication : ProtocolApplication() {
    override fun getCompany(): Company {
        return Company.AAVE
    }
}

fun main(args: Array<String>) {
    runApplication<AaveApplication>(*args)
}