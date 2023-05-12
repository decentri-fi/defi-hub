package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class BancorApplication : ProtocolApplication() {

    override fun getCompany(): Company {
        return Company.BANCOR
    }
}

fun main(args: Array<String>) {
    runApplication<BancorApplication>(*args)
}