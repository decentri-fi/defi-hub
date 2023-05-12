package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class DodoApplication : ProtocolApplication() {
    override fun getCompany(): Company {
        return Company.DODO
    }
}

fun main(args: Array<String>) {
    runApplication<DodoApplication>(*args)
}