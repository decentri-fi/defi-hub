package io.defitrack.protocol.convex.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import org.web3j.abi.datatypes.Function

class CvxRewardPoolContract(
    solidityBasedContractAccessor: BlockchainGateway,
    address: String,
) : ERC20Contract(solidityBasedContractAccessor, address) {


    val rewardToken = constant<String>("rewardToken", address())
    val stakingToken = constant<String>("stakingToken", address())

    fun getRewardFunction(): MutableFunction {
        return createFunction(
            "getReward",
            listOf()
        ).toMutableFunction()
    }

    fun earnedFunction(user: String): Function {
        return createFunction(
            "earned",
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }
}