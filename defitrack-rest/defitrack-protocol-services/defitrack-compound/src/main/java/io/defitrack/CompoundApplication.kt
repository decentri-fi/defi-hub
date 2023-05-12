package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class CompoundApplication : ProtocolApplication() {
    override fun getCompany(): Company {
        return Company.COMPOUND
    }
}

fun main(args: Array<String>) {
    runApplication<CompoundApplication>(*args)
}