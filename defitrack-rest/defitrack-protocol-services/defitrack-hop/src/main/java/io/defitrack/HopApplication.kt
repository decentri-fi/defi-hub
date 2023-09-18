package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.ApplicationContext

class HopApplication(applicationContext: ApplicationContext) : ProtocolApplication(
    applicationContext
) {
    override fun getCompany(): Company {
        return Company.HOP
    }
}

fun main(args: Array<String>) {
    runApplication<HopApplication>(*args)
}