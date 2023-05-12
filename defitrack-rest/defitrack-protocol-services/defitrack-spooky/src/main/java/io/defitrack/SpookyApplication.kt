package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class SpookyApplication : ProtocolApplication() {

    override fun getCompany(): Company {
        return Company.SPOOKY
    }
}

fun main(args: Array<String>) {
    runApplication<SpookyApplication>(*args)
}