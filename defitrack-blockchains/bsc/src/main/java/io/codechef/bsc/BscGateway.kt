package io.codechef.bsc

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.web3j.protocol.Web3j
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Component
class BscGateway(
    val bscWeb3jConfiguerer: BscWeb3jConfigurer,
    @Qualifier("bscWeb3j") val web3j: Web3j,
) {

    @Scheduled(fixedRate = 20000)
    fun scheduledTask() {
        try {
            bscWeb3jConfiguerer.assureConnection()
        } catch (ex: Exception) {
            logger.error("Unable to reconnect ethereum websocket", ex)
        }
    }

    fun web3j(): Web3j {
        bscWeb3jConfiguerer.assureConnection()
        return web3j
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}