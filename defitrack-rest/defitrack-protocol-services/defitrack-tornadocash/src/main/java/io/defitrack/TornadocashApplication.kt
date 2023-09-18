package io.defitrack

import io.defitrack.protocol.Company
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext

class TornadocashApplication(applicationContext: ApplicationContext) : ProtocolApplication(
    applicationContext
) {
    override fun getCompany(): Company {
        return Company.TORNADO_CASH
    }
}

fun main(args: Array<String>) {
    runApplication<TornadocashApplication>(*args)
}