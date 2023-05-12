package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.mockito.internal.debugging.WarningsPrinterImpl
import org.springframework.boot.runApplication

class WepiggyApplication : ProtocolApplication() {
    override fun getCompany(): Company {
        return Company.WEPIGGY
    }
}

fun main(args: Array<String>) {
    runApplication<WepiggyApplication>(*args)
}