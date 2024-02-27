package io.defitrack.protocol.mycelium

import io.defitrack.abi.TypeUtils
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.DeprecatedEvmContract

class TcrStakingRewards(blockchainGateway: BlockchainGateway, address: String) : DeprecatedEvmContract(
    blockchainGateway, address
) {

    val rewardsToken = constant<String>("rewardsToken", TypeUtils.address())
    val stakingToken = constant<String>("stakingToken", TypeUtils.address())

    fun exitFn(): ContractCall {
        return createFunction("exit")
    }
}