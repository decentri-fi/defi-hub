package io.defitrack.protocol.balancer.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.DeprecatedEvmContract
import org.web3j.abi.datatypes.Event

class BalancerPoolFactoryContract(
    blockchainGateway: BlockchainGateway, address: String
) : DeprecatedEvmContract(
    blockchainGateway, address
) {

    companion object {
        val POOL_CREATED_EVENT = Event(
            "PoolCreated",
            listOf(address(true))
        )
    }
}