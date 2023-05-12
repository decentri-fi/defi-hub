package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class StargateApplication : ProtocolApplication() {
    override fun getCompany(): Company {
        return Company.STARGATE
    }
}

fun main(args: Array<String>) {
    runApplication<StargateApplication>(*args)
}