package io.defitrack

import io.defitrack.protocol.Company
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext

class MoonwellApplication(applicationContext: ApplicationContext) : ProtocolApplication(applicationContext) {
    override fun getCompany(): Company {
        return Company.MOONWELL
    }
}

fun main(args: Array<String>) {
    runApplication<MoonwellApplication>(*args)
}