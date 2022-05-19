package io.defitrack.erc20

import io.defitrack.common.network.Network
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.annotation.PostConstruct

@Configuration
@Profile("!dev")
class DataPopulator(private val tokenService: TokenService) {

    val logger = LoggerFactory.getLogger(this::class.java)

    @PostConstruct
    fun start() {
        Executors.newSingleThreadExecutor().submit {
            runBlocking {
                Network.values()
                    .filter {
                        it.hasMicroService
                    }.map {
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
        }
    }
}