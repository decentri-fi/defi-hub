package io.defitrack

import io.defitrack.protocol.Company
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class PoolTogetherApplication : ProtocolApplication() {
    override fun getCompany(): Company {
        return Company.POOLTOGETHER
    }
}

fun main(args: Array<String>) {
    runApplication<PoolTogetherApplication>(*args)
}