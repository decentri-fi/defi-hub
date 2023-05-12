package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class ChainlinkApplication : ProtocolApplication() {
    override fun getCompany(): Company {
        return Company.CHAINLINK
    }
}

fun main(args: Array<String>) {
    runApplication<ChainlinkApplication>(*args)
}