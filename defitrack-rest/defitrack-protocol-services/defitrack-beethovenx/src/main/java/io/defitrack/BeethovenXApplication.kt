package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class BeethovenXApplication : ProtocolApplication() {

    override fun getCompany(): Company {
        return Company.BEETHOVENX
    }
}

fun main(args: Array<String>) {
    runApplication<BeethovenXApplication>(*args)
}