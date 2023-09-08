package io.defitrack

import io.defitrack.protocol.Company
import org.springframework.boot.runApplication

class StakefishApplication : ProtocolApplication() {

    override fun getCompany(): Company {
        return Company.STAKEFISH
    }
}

fun main(args: Array<String>) {
    runApplication<StakefishApplication>(*args)
}