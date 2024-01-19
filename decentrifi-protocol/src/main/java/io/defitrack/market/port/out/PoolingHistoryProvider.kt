package io.defitrack.market.port.out

import io.defitrack.common.network.Network
import io.defitrack.networkinfo.toNetworkInformation
import io.defitrack.event.DefiEvent
import io.defitrack.event.DefiEventType
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.Protocol
import org.springframework.beans.factory.annotation.Autowired
import org.web3j.protocol.core.methods.response.Log

abstract class PoolingHistoryProvider {

    @Autowired
    protected lateinit var blockchainGatewayProvider: BlockchainGatewayProvider

    suspend fun getTransaction(network: Network, txId: String): BlockchainGateway.TransactionVO {
        return getGateway(network).getTransaction(txId)
            ?: throw IllegalArgumentException("Invalid transaction $txId for network $network")
    }

    fun getGateway(network: Network): BlockchainGateway {
        return blockchainGatewayProvider.getGateway(network)
    }

    suspend fun event(
        log: Log,
        network: Network,
        type: DefiEventType,
        protocol: Protocol? = null,
        metadata: Map<String, Any> = emptyMap()
    ): DefiEvent {

        val transaction = getTransaction(network, log.transactionHash)
        val id = network.slug + "_" + transaction.hash + "-" + log.logIndex

        return DefiEvent(
            id = id,
            protocol = protocol,
            transaction = getTransaction(network, log.transactionHash),
            type = type,
            metadata = metadata,
            network = network.toNetworkInformation()
        )
    }

}