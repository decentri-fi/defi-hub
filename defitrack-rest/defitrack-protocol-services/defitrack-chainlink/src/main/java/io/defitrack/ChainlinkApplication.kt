package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext

class ChainlinkApplication(applicationContext: ApplicationContext) : ProtocolApplication(
    applicationContext
) {
    override fun getCompany(): Company {
        return Company.CHAINLINK
    }
}

fun main(args: Array<String>) {
    runApplication<ChainlinkApplication>(*args)
}