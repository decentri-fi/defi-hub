package io.defitrack

import io.defitrack.protocol.Company
import org.springframework.boot.runApplication

class VelodromeApplication : ProtocolApplication() {
    override fun getCompany(): Company {
        return Company.VELODROME
    }
}

fun main(args: Array<String>) {
    runApplication<VelodromeApplication>(*args)
}