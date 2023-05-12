package io.defitrack

import io.defitrack.protocol.Company
import org.springframework.boot.runApplication

class RibbonAplication : ProtocolApplication() {

    override fun getCompany(): Company {
        return Company.RIBBON
    }
}

fun main(args: Array<String>) {
    runApplication<RibbonAplication>(*args)
}