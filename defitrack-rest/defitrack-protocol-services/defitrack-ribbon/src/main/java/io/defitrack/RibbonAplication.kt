package io.defitrack

import io.defitrack.protocol.Company
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext

class RibbonAplication(applicationContext: ApplicationContext) : ProtocolApplication(
    applicationContext
) {

    override fun getCompany(): Company {
        return Company.RIBBON
    }
}

fun main(args: Array<String>) {
    runApplication<RibbonAplication>(*args)
}