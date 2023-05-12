package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class SolidLizardApp : ProtocolApplication() {
    override fun getCompany(): Company {
        return Company.SOLIDLIZARD
    }
}

fun main(args: Array<String>) {
    runApplication<SolidLizardApp>(*args)
}