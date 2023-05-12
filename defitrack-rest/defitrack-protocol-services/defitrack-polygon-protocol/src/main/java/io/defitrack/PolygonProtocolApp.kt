package io.defitrack

import io.defitrack.protocol.Company
import org.springframework.boot.runApplication

class PolygonProtocolApp : ProtocolApplication() {
    override fun getCompany(): Company {
        return Company.POLYGON
    }
}

fun main(args: Array<String>) {
    runApplication<PolygonProtocolApp>(*args)
}