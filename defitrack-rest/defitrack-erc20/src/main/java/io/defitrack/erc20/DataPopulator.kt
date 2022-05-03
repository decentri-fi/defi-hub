package io.defitrack.erc20

import io.defitrack.common.network.Network
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

@Configuration
class DataPopulator(private val tokenService: TokenService) {

    val logger = LoggerFactory.getLogger(this::class.java)

    @PostConstruct
    fun start() {
        runBlocking {
            val jobs = Network.values().map {
                launch {
                    try {
                        logger.info("Initial population for $it")
                        tokenService.getAllTokensForNetwork(it)
                        logger.info("Done populating $it")
                    } catch (ex: Exception) {
                        logger.error("${ex.message}")
                    }
                }
            }

            jobs.joinAll()
        }
        logger.info("done populating everything")
    }
}