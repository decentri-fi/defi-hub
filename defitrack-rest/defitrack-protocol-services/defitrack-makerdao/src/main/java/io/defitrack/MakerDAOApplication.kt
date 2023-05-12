package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class MakerDAOApplication : ProtocolApplication() {
    override fun getCompany(): Company {
        return Company.MAKERDAO
    }
}

fun main(args: Array<String>) {
    runApplication<MakerDAOApplication>(*args)
}