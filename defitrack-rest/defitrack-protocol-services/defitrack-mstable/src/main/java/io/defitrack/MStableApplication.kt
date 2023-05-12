package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class MStableApplication : ProtocolApplication() {

    override fun getCompany(): Company {
        return Company.MSTABLE
    }
}

fun main(args: Array<String>) {
    runApplication<MStableApplication>(*args)
}