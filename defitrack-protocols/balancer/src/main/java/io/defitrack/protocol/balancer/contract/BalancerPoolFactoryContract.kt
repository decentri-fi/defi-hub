package io.defitrack.protocol.balancer.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Event

class BalancerPoolFactoryContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(
    blockchainGateway, "", address
) {

    companion object {
        val POOL_CREATED_EVENT = Event(
            "PoolCreated",
            listOf(TypeUtils.address(true))
        )
    }
}