package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class IronBankApplication : ProtocolApplication() {
    override fun getCompany(): Company {
        return Company.IRON_BANK
    }
}

fun main(args: Array<String>) {
    runApplication<IronBankApplication>(*args)
}