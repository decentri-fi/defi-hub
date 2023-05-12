package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class OlympusDAOApplication : ProtocolApplication() {

    override fun getCompany(): Company {
        return Company.OLYMPUSDAO
    }
}

fun main(args: Array<String>) {
    runApplication<OlympusDAOApplication>(*args)
}