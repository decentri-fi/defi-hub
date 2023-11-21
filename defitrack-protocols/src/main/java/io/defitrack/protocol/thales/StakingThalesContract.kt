package io.defitrack.protocol.thales


import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function

class StakingThalesContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(blockchainGateway, address) {

    val stakingToken = constant<String>("stakingToken", TypeUtils.address())

    fun stakedBalanceOfFn(user: String): Function {
        return createFunction(
            "stakedBalanceOf",
            listOf(user.toAddress()),
            listOf(TypeUtils.uint256())
        )
    }

    fun getRewardsAvailableFn(user: String): Function {
        return createFunction(
            "getRewardsAvailable",
            listOf(user.toAddress()),
            listOf(TypeUtils.uint256())
        )
    }

    fun claimRewardFn(user: String): MutableFunction {
        return createFunction(
            "claimReward",
            listOf()
        ).toMutableFunction()
    }
}