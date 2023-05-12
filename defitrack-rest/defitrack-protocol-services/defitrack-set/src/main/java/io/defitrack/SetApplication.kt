package io.defitrack

import io.defitrack.protocol.Company
import org.springframework.boot.runApplication

class SetApplication : ProtocolApplication() {
    override fun getCompany(): Company {
        return Company.SET
    }
}

fun main(args: Array<String>) {
    runApplication<SetApplication>(*args)
}