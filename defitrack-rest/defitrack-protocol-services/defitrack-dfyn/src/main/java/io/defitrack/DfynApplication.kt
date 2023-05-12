package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class DfynApplication : ProtocolApplication() {

    override fun getCompany(): Company {
        return Company.DFYN
    }
}

fun main(args: Array<String>) {
    runApplication<DfynApplication>(*args)
}