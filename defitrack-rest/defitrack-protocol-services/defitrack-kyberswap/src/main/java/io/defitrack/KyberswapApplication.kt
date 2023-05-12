package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class KyberswapApplication : ProtocolApplication() {

    override fun getCompany(): Company {
        return Company.KYBER_SWAP
    }
}

fun main(args: Array<String>) {
    runApplication<KyberswapApplication>(*args)
}