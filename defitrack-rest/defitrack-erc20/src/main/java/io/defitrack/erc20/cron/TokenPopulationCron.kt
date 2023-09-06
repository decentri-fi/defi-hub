package io.defitrack.erc20.cron

import io.defitrack.erc20.ERC20Repository
import io.defitrack.erc20.ERC20Service
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Executors

@Configuration
@ConditionalOnProperty("token-population.enabled", havingValue = "true")
class TokenPopulationCron(
    private val erC20Repository: ERC20Repository,
    private val ERC20Service: ERC20Service
) {

    val logger = LoggerFactory.getLogger(this::class.java)
    val executor = Executors.newSingleThreadExecutor()

    @PostConstruct
    fun run() {
        executor.submit {
            runBlocking {
                logger.info("Starting token population")
                erC20Repository.populateTokens()
                ERC20Service.initialPopulation()
                logger.info("end of token population")
            }
        }
    }
}