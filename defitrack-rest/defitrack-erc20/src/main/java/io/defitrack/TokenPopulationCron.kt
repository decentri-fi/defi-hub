package io.defitrack

import io.defitrack.erc20.ERC20Repository
import io.defitrack.erc20.TokenService
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Executors

@Configuration
class TokenPopulationCron(
    private val erC20Repository: ERC20Repository,
    private val tokenService: TokenService
) {

    val logger = LoggerFactory.getLogger(this::class.java)
    val executor = Executors.newSingleThreadExecutor()

    @PostConstruct
    fun run() {
        executor.submit {
            runBlocking {
                logger.info("Starting token population")
                erC20Repository.populateTokens()
                tokenService.initialPopulation()
                logger.info("end of token population")
            }
        }
    }
}