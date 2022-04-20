package io.defitrack.contract

import io.defitrack.evm.web3j.EvmGateway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.web3j.protocol.Web3j

class BlockchainGateway(
    val provider: Web3JProvider,
    val web3j: Web3j,
) : EvmGateway {

    @Scheduled(fixedRate = 20000)
    fun scheduledTask() {
        try {
            provider.assureConnection()
        } catch (ex: Exception) {
            logger.error("Unable to reconnect ethereum websocket", ex)
        }
    }

    override fun web3j(): Web3j {
        provider.assureConnection()
        return web3j
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}