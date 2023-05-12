package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class BlurApplication : ProtocolApplication() {
    override fun getCompany(): Company {
        return Company.BLUR
    }
}

fun main(args: Array<String>) {
    runApplication<BlurApplication>(*args)
}