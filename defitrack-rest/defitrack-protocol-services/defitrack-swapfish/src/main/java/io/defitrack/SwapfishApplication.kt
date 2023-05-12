package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class SwapfishApplication : ProtocolApplication() {
    override fun getCompany(): Company {
        return Company.SWAPFISH
    }
}

fun main(args: Array<String>) {
    runApplication<SwapfishApplication>(*args)
}