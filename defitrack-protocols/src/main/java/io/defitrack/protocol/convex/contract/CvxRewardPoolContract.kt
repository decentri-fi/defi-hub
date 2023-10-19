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


    suspend fun rewardToken(): String {
        return readSingle("rewardToken", address())
    }

    fun getRewardFunction(): ContractCall {
        return createFunction(
            "getReward",
            listOf()
        ).toContractCall()
    }

    fun earnedFunction(user: String): Function {
        return createFunction(
            "earned",
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }

    suspend fun stakingToken(): String {
        return readSingle("stakingToken", address())
    }
}