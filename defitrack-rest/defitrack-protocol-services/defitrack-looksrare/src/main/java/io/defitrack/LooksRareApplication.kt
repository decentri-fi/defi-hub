package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication

class LooksRareApplication : ProtocolApplication() {

    override fun getCompany(): Company {
        return Company.LOOKSRARE
    }
}

fun main(args: Array<String>) {
    runApplication<LooksRareApplication>(*args)
}