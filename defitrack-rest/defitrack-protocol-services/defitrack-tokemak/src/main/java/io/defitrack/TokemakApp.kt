package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class TokemakApp : ProtocolApplication() {
    override fun getCompany(): Company {
        return Company.TOKEMAK
    }
}

fun main(args: Array<String>) {
    runApplication<TokemakApp>(*args)
}