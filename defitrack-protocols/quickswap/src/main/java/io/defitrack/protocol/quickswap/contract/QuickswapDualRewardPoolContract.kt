package io.defitrack.protocol.quickswap.contract

import io.defitrack.abi.TypeUtils.Companion.address
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
        return readSingle("rewardsTokenA", address())
    }

    suspend fun rewardsTokenAddressB(): String {
        return readSingle("rewardsTokenB", address())
    }

    suspend fun stakingTokenAddress(): String {
        return readSingle("stakingToken", address());
    }

    suspend fun rewardRateA(): BigInteger {
        return readSingle("rewardRateA", uint256())
    }


    suspend fun periodFinish(): BigInteger {
        return readSingle("periodFinish", uint256())
    }

    suspend fun rewardRateB(): BigInteger {
        return readSingle("rewardRateB", uint256())
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