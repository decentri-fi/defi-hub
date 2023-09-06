package io.defitrack

import io.defitrack.protocol.Company
import org.springframework.boot.runApplication

class GmxApplication : ProtocolApplication() {
    override fun getCompany(): Company {
        return Company.GMX
    }
}

fun main(args: Array<String>) {
    runApplication<GmxApplication>(*args)
}