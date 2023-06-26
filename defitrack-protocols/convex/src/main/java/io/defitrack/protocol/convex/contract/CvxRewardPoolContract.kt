package io.defitrack.protocol.convex.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import org.web3j.abi.datatypes.Function

class CvxRewardPoolContract(
    solidityBasedContractAccessor: BlockchainGateway,
    abi: String,
    address: String,
) : ERC20Contract(solidityBasedContractAccessor, abi, address) {

    suspend fun rewardToken(): String {
        return readWithoutAbi(
            "rewardToken",
            emptyList(),
            listOf(TypeUtils.address())
        )[0].value as String
    }

    fun getRewardFunction(user: String): Function {
        return createFunction(
            "getReward",
            listOf()
        )
    }

    fun earnedFunction(user: String): Function {
        return createFunction(
            "earned",
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }

    suspend fun stakingToken(): String {
        return readWithAbi(
            "stakingToken",
            outputs = listOf(address())
        )[0].value as String
    }
}