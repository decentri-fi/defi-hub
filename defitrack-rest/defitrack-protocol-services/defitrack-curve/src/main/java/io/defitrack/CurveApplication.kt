package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class CurveApplication : ProtocolApplication() {

    override fun getCompany(): Company {
        return Company.CURVE
    }
}

fun main(args: Array<String>) {
    runApplication<CurveApplication>(*args)
}