package io.defitrack.erc20.adapter.tokens

import io.defitrack.erc20.application.ERC20TokenService
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.availability.AvailabilityChangeEvent
import org.springframework.boot.availability.ReadinessState
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.Executors

@Configuration
@ConditionalOnProperty("token-population.enabled", havingValue = "true")
@ConditionalOnBean(ERC20TokenExternalListResource::class)
class TokenPopulationCron(
    private val resource: ERC20TokenExternalListResource,
    private val erc20TokenService: ERC20TokenService,
    private val applicationContext: ApplicationContext
) {

    val logger = LoggerFactory.getLogger(this::class.java)


    @PostConstruct
    fun run() {
        runBlocking {
            logger.info("Starting token population")
            resource.populateTokens()
            erc20TokenService.initialPopulation()
            logger.info("end of token population")
            AvailabilityChangeEvent.publish(applicationContext, ReadinessState.ACCEPTING_TRAFFIC)
        }
    }

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 3, initialDelay = 1000 * 60 * 60 * 3)
    fun refreshCaches() = runBlocking {
        erc20TokenService.refreshCache()
    }
}