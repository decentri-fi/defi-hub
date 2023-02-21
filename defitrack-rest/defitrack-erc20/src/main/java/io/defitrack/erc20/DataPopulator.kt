package io.defitrack.erc20

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled

@Configuration
class DataPopulator(private val tokenService: TokenService) {

    val logger = LoggerFactory.getLogger(this::class.java)

    fun refreshCaches() {
        tokenService.refreshCache()
    }
}