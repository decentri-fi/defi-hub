package io.defitrack

import io.defitrack.protocol.Company
import org.springframework.boot.runApplication

class AlienbaseApplication : ProtocolApplication() {
    override fun getCompany(): Company {
        return Company.ALIENBASE
    }
}

fun main(args: Array<String>) {
    runApplication<AlienbaseApplication>(*args)
}