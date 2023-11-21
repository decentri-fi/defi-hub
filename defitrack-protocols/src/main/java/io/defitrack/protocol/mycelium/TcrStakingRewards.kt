package io.defitrack.protocol.mycelium

import io.defitrack.abi.TypeUtils
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract

class TcrStakingRewards(blockchainGateway: BlockchainGateway, address: String) : EvmContract(
    blockchainGateway, address
) {

    val rewardsToken = constant<String>("rewardsToken", TypeUtils.address())
    val stakingToken = constant<String>("stakingToken", TypeUtils.address())

    fun exitFn(): MutableFunction {
        return createFunction(
            "exit",
            emptyList(),
            emptyList()
        ).toMutableFunction()
    }
}