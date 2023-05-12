package io.defitrack

import io.defitrack.protocol.Company
import org.springframework.boot.runApplication

class TornadocashApplication : ProtocolApplication() {
    override fun getCompany(): Company {
        return Company.TORNADO_CASH
    }
}

fun main(args: Array<String>) {
    runApplication<TornadocashApplication>(*args)
}