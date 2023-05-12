package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class ApeswapAplication : ProtocolApplication() {

    override fun getCompany(): Company {
        return Company.APESWAP
    }
}

fun main(args: Array<String>) {
    runApplication<ApeswapAplication>(*args)
}