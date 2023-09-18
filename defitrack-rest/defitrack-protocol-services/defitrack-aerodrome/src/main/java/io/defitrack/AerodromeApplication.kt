package io.defitrack

import io.defitrack.protocol.Company
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext

class AerodromebaseApplication(applicationContext: ApplicationContext) : ProtocolApplication(
    applicationContext
) {
    override fun getCompany(): Company {
        return Company.AERODROME
    }
}

fun main(args: Array<String>) {
    runApplication<AerodromebaseApplication>(*args)
}