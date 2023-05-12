package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

class HopApplication : ProtocolApplication() {
    override fun getCompany(): Company {
        return Company.HOP
    }
}

fun main(args: Array<String>) {
    runApplication<HopApplication>(*args)
}