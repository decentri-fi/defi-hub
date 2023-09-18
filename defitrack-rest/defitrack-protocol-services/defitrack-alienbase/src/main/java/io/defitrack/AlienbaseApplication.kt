package io.defitrack

import io.defitrack.protocol.Company
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext

class AlienbaseApplication(applicationContext: ApplicationContext) : ProtocolApplication(
    applicationContext
) {
    override fun getCompany(): Company {
        return Company.ALIENBASE
    }
}

fun main(args: Array<String>) {
    runApplication<AlienbaseApplication>(*args)
}