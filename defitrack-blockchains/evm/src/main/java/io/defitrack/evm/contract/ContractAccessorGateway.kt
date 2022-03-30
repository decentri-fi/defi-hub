package io.defitrack.evm.contract

import io.defitrack.common.network.Network
import org.springframework.stereotype.Component

@Component
class ContractAccessorGateway(private val evmContractAccessor: List<EvmContractAccessor>) {

    fun getGateway(network: Network): EvmContractAccessor {
        return evmContractAccessor.firstOrNull {
            it.network == network
        } ?: throw java.lang.IllegalArgumentException("gateway for network $network was not found")
    }
}