package io.defitrack.evm.contract

import io.defitrack.common.network.Network
import org.springframework.stereotype.Component

@Component
class ContractAccessorGateway(private val blockchainGateway: List<BlockchainGateway>) {

    fun getGateway(network: Network): BlockchainGateway {
        return blockchainGateway.firstOrNull {
            it.network == network
        } ?: throw java.lang.IllegalArgumentException("gateway for network $network was not found")
    }
}