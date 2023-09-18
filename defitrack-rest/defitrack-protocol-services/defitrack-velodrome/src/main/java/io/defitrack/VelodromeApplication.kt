package io.defitrack

import io.defitrack.protocol.Company
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext

class VelodromeApplication(applicationContext: ApplicationContext) : ProtocolApplication(
    applicationContext
) {
    override fun getCompany(): Company {
        return Company.VELODROME
    }
}

fun main(args: Array<String>) {
    runApplication<VelodromeApplication>(*args)
}