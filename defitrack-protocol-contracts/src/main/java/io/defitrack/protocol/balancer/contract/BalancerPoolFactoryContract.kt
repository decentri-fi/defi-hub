package io.defitrack.protocol.balancer.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Event

context(BlockchainGateway)
class BalancerPoolFactoryContract(
    address: String
) : EvmContract(address) {

    companion object {
        val POOL_CREATED_EVENT = Event(
            "PoolCreated",
            listOf(address(true))
        )
    }
}