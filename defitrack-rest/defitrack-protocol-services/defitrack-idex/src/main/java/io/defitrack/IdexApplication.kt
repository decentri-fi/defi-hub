package io.defitrack

import io.defitrack.protocol.Company
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableScheduling

class IdexApplication :   ProtocolApplication() {
    override fun getCompany(): Company {
        return Company.IDEX
    }
}

fun main(args: Array<String>) {
    runApplication<IdexApplication>(*args)
}