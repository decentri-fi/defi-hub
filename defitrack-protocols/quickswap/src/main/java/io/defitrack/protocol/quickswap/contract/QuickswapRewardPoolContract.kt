package io.defitrack.protocol.quickswap.contract

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

class QuickswapRewardPoolContract(
    solidityBasedContractAccessor: BlockchainGateway,
    abi: String,
    address: String,
) : ERC20Contract(
    solidityBasedContractAccessor,
    abi, address
) {

    suspend fun stakingTokenAddress(): String {
        return readWithAbi(
            method = "stakingToken",
            outputs = listOf(TypeReference.create(Address::class.java))
        )[0].value as String
    }

    suspend fun rewardsTokenAddress(): String {
        return read("rewardsToken")
    }

    suspend fun rewardRate(): BigInteger {
        return read("rewardRate")
    }

    fun getRewardFunction(): Function {
        return createFunction("getReward")
    }

    fun earned(address: String): Function {
        return createFunction(
            "earned",
            listOf(address.toAddress()),
            listOf(uint256())
        )
    }
}