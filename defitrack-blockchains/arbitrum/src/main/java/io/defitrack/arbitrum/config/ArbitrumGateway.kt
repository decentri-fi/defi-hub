package io.defitrack.arbitrum.config

import io.defitrack.evm.web3j.EvmGateway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.web3j.protocol.Web3j

@Component
class ArbitrumGateway(
    val abstractWeb3JConfigurer: ArbitrumWeb3jConfigurer,
    @Qualifier("arbitrumWeb3j") val web3j: Web3j,
) : EvmGateway {

    @Scheduled(fixedRate = 20000)
    fun scheduledTask() {
        try {
            abstractWeb3JConfigurer.assureConnection()
        } catch (ex: Exception) {
            logger.error("Unable to reconnect arbitrum websocket", ex)
        }
    }

    override fun web3j(): Web3j {
        abstractWeb3JConfigurer.assureConnection()
        return web3j
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}