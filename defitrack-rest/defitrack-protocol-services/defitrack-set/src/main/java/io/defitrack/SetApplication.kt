package io.defitrack

import io.defitrack.protocol.Company
import org.springframework.boot.availability.AvailabilityChangeEvent
import org.springframework.boot.availability.ReadinessState
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext

class SetApplication(applicationContext: ApplicationContext) : ProtocolApplication(applicationContext) {

    override fun getCompany(): Company {
        return Company.SET
    }
}

fun main(args: Array<String>) {
    runApplication<SetApplication>(*args)
}