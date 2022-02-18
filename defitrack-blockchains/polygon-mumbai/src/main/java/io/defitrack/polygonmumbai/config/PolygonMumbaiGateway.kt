package io.defitrack.polygonmumbai.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.annotation.Order
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.web3j.protocol.Web3j

@Component
@Order(2)
class PolygonMumbaiGateway(
    val abstractWeb3JConfigurer: PolygonMumbaiWeb3jConfigurer,
    @Qualifier("polygonMumbaiWeb3j") val web3j: Web3j,
) {

    @Scheduled(fixedRate = 20000)
    fun scheduledTask() {
        try {
            abstractWeb3JConfigurer.assureConnection()
        } catch (ex: Exception) {
            logger.error("Unable to reconnect ethereum websocket", ex)
        }
    }

    fun web3j(): Web3j {
        abstractWeb3JConfigurer.assureConnection()
        return web3j
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}