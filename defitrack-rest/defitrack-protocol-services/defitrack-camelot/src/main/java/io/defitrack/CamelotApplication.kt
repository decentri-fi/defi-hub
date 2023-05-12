package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class CamelotApplication : ProtocolApplication() {
    override fun getCompany(): Company {
        return Company.CAMELOT
    }
}

fun main(args: Array<String>) {
    runApplication<CamelotApplication>(*args)
}