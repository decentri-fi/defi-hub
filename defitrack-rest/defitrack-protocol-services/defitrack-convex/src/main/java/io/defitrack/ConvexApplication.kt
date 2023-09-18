package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext

class ConvexApplication(applicationContext: ApplicationContext) : ProtocolApplication(
    applicationContext
) {
    override fun getCompany(): Company {
        return Company.CONVEX
    }
}

fun main(args: Array<String>) {
    runApplication<ConvexApplication>(*args)
}