package io.defitrack.protocol.thales


import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract

class StakingThalesContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(blockchainGateway, address) {

    val stakingToken = constant<String>("stakingToken", TypeUtils.address())

    fun stakedBalanceOfFn(user: String): ContractCall {
        return createFunction(
            "stakedBalanceOf",
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }

    fun getRewardsAvailableFn(user: String): ContractCall {
        return createFunction(
            "getRewardsAvailable",
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }

    fun claimRewardFn(): ContractCall {
        return createFunction(
            "claimReward",
            listOf()
        )
    }
}