package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext

class KyberswapApplication(applicationContext: ApplicationContext) : ProtocolApplication(
    applicationContext
) {

    override fun getCompany(): Company {
        return Company.KYBER_SWAP
    }
}

fun main(args: Array<String>) {
    runApplication<KyberswapApplication>(*args)
}