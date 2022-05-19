package io.defitrack.erc20

import io.defitrack.common.network.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import javax.annotation.PostConstruct

@Configuration
@Profile("!dev")
class DataPopulator(private val tokenService: TokenService) {

    val logger = LoggerFactory.getLogger(this::class.java)

    @PostConstruct
    fun start() {
        runBlocking {
            val jobs = Network.values()
                .filter {
                    it.hasMicroService
                }
                .map {
                    launch(Dispatchers.IO.limitedParallelism(3)) {
                        try {
                            logger.info("Initial population for $it")
                            val tokens = tokenService.getAllTokensForNetwork(it)
                            logger.info("Done populating $it with ${tokens.size} elements")
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                            logger.error("${ex.message}")
                        }
                    }
                }

            jobs.joinAll()
        }
        logger.info("done populating everything")
    }
}