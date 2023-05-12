package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class PolycatApplication : ProtocolApplication() {
    override fun getCompany(): Company {
        return Company.POLYCAT
    }
}

fun main(args: Array<String>) {
    runApplication<PolycatApplication>(*args)
}