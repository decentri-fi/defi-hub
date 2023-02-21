package io.defitrack.protocol.quickswap.contract

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import java.math.BigInteger

class QuickswapDualRewardPoolContract(
    solidityBasedContractAccessor: BlockchainGateway,
    abi: String,
    address: String,
) : ERC20Contract(
    solidityBasedContractAccessor,
    abi, address
) {

    suspend fun rewardsTokenAddressA(): String {
        return readWithAboi("rewardsTokenA")
    }

    suspend fun rewardsTokenAddressB(): String {
        return readWithAboi("rewardsTokenB")
    }

    suspend fun stakingTokenAddress(): String {
        return readWithAboi("stakingToken");
    }

    suspend fun rewardRateA(): BigInteger {
        return readWithAbi(
            method = "rewardRateA",
            outputs = listOf(uint256())
        )[0].value as BigInteger
    }


    suspend fun rewardRateB(): BigInteger {
        return readWithAbi(
            method = "rewardRateB",
            outputs = listOf(uint256())
        )[0].value as BigInteger
    }

    suspend fun earnedA(address: String): BigInteger {
        return readWithAbi(
            "earnedA",
            listOf(address.toAddress()),
            listOf(uint256())
        )[0].value as BigInteger
    }

    suspend fun earnedB(address: String): BigInteger {
        return readWithAbi(
            "earnedB",
            listOf(address.toAddress()),
            listOf(uint256())
        )[0].value as BigInteger
    }
}