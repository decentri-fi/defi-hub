package io.defitrack.protocol.convex.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.ERC20Contract

context(BlockchainGateway)
class CvxRewardPoolContract(address: String) : ERC20Contract(address) {


    val rewardToken = constant<String>("rewardToken", address())
    val stakingToken = constant<String>("stakingToken", address())

    fun getRewardFunction(): ContractCall {
        return createFunction("getReward")
    }

    fun earnedFunction(user: String): ContractCall {
        return createFunction(
            "earned",
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }
}