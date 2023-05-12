package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class AdamantApplication : ProtocolApplication() {

    override fun getCompany(): Company {
        return Company.ADAMANT
    }
}

fun main(args: Array<String>) {
    runApplication<AdamantApplication>(*args)
}