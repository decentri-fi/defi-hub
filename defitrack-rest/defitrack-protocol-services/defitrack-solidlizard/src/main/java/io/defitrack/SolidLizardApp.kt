package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext

class SolidLizardApp(applicationContext: ApplicationContext) : ProtocolApplication(
    applicationContext
) {
    override fun getCompany(): Company {
        return Company.SOLIDLIZARD
    }
}

fun main(args: Array<String>) {
    runApplication<SolidLizardApp>(*args)
}