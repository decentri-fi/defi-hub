package io.defitrack.protocol.quickswap.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

class QuickswapRewardPoolContract(
    solidityBasedContractAccessor: BlockchainGateway,
    address: String,
) : ERC20Contract(
    solidityBasedContractAccessor, address
) {

    fun exitFunction(amount: BigInteger): Function {
        return createFunction(
            "leave",
            listOf(amount.toUint256()),
            emptyList()
        )
    }

    suspend fun periodFinish(): BigInteger {
        return readSingle("periodFinish", uint256())
    }

    suspend fun stakingTokenAddress(): String {
        return readSingle("stakingToken", address())
    }

    suspend fun rewardsTokenAddress(): String {
        return readSingle("rewardsToken", address())
    }

    suspend fun rewardRate(): BigInteger {
        return readSingle("rewardRate", address())
    }

    fun getRewardFunction(): ContractCall {
        return createFunction("getReward").toContractCall()
    }

    fun earned(address: String): Function {
        return createFunction(
            "earned",
            listOf(address.toAddress()),
            listOf(uint256())
        )
    }
}