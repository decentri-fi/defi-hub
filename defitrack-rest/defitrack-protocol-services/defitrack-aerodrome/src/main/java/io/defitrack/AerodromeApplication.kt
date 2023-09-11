package io.defitrack

import io.defitrack.protocol.Company
import org.springframework.boot.runApplication

class AerodromebaseApplication : ProtocolApplication() {
    override fun getCompany(): Company {
        return Company.AERODROME
    }
}

fun main(args: Array<String>) {
    runApplication<AerodromebaseApplication>(*args)
}