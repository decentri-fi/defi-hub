package io.defitrack.protocol.aura

import io.defitrack.abi.TypeUtils
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EventUtils.extract
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Event

context(BlockchainGateway)
class RewardPoolFactoryContract(
    address: String
) : EvmContract(
    address
) {

    val poolCreatedEvent = Event(
        "RewardPoolCreated", listOf(
            TypeUtils.address(),
            TypeUtils.uint256(),
            TypeUtils.address(),
        )
    )

    suspend fun getCreatedPools(fromBlock: String, toBlock: String? = null): List<String> {
        return getLogs(poolCreatedEvent, fromBlock, toBlock)
            .map {
                poolCreatedEvent.extract(it, false, 0) as String
            }
    }
}