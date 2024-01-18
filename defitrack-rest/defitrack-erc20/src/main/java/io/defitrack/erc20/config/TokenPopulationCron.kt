package io.defitrack.erc20.config

import io.defitrack.erc20.application.repository.ERC20Repository
import io.defitrack.erc20.application.ERC20TokenService
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.Executors

@Configuration
@ConditionalOnProperty("token-population.enabled", havingValue = "true")
class TokenPopulationCron(
    private val erC20Repository: ERC20Repository,
    private val erc20TokenService: ERC20TokenService
) {

    val logger = LoggerFactory.getLogger(this::class.java)
    val executor = Executors.newSingleThreadExecutor()

    @PostConstruct
    fun run() {
        executor.submit {
            runBlocking {
                logger.info("Starting token population")
                erC20Repository.populateTokens()
                erc20TokenService.initialPopulation()
                logger.info("end of token population")
            }
        }
    }

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 3, initialDelay = 1000 * 60 * 60 * 3)
    fun refreshCaches() = runBlocking {
        erc20TokenService.refreshCache()
    }
}