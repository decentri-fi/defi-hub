package io.defitrack

import io.defitrack.protocol.distribution.ProtocolDistributionConfig
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DefitrackApiGwApplication(
    private val protocolDistributionConfig: ProtocolDistributionConfig
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostConstruct
    fun init() = runBlocking {
        logger.info("startup with distsributionconfig")
        protocolDistributionConfig.getConfigs().forEach {
            logger.info("${it.name}: ${it.companies.joinToString(",") { it.slug }}")
        }
    }
}

fun main(args: Array<String>) {
    runApplication<DefitrackApiGwApplication>(*args)
}