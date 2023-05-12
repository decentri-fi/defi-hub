package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class BalancerApplication : ProtocolApplication() {

    override fun getCompany(): Company {
        return Company.BALANCER
    }
}

fun main(args: Array<String>) {
    runApplication<BalancerApplication>(*args)
}