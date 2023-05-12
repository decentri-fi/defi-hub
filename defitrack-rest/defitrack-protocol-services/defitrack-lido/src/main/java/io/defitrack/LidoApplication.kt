package io.defitrack

import io.defitrack.protocol.Company
import org.springframework.boot.runApplication

class LidoApplication : ProtocolApplication() {

    override fun getCompany(): Company {
        return Company.LIDO
    }
}

fun main(args: Array<String>) {
    runApplication<LidoApplication>(*args)
}