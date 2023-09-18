package io.defitrack

import io.defitrack.protocol.Company
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext

class LidoApplication(applicationContext: ApplicationContext) : ProtocolApplication(
    applicationContext
) {

    override fun getCompany(): Company {
        return Company.LIDO
    }
}

fun main(args: Array<String>) {
    runApplication<LidoApplication>(*args)
}