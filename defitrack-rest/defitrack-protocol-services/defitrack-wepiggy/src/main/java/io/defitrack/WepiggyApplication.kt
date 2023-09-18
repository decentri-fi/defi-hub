package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.mockito.internal.debugging.WarningsPrinterImpl
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext

class WepiggyApplication(applicationContext: ApplicationContext) : ProtocolApplication(
    applicationContext
) {
    override fun getCompany(): Company {
        return Company.WEPIGGY
    }
}

fun main(args: Array<String>) {
    runApplication<WepiggyApplication>(*args)
}