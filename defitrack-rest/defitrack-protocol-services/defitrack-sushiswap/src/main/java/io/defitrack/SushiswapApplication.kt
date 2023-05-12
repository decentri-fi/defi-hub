package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableAsync

class SushiswapApplication : ProtocolApplication() {
    override fun getCompany(): Company {
        return Company.SUSHISWAP
    }
}

fun main(args: Array<String>) {
    runApplication<SushiswapApplication>(*args)
}