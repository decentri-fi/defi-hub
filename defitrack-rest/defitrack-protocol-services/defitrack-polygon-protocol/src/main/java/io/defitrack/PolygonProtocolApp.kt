package io.defitrack

import io.defitrack.protocol.Company
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext

class PolygonProtocolApp(applicationContext: ApplicationContext) : ProtocolApplication(
    applicationContext
) {
    override fun getCompany(): Company {
        return Company.POLYGON
    }
}

fun main(args: Array<String>) {
    runApplication<PolygonProtocolApp>(*args)
}