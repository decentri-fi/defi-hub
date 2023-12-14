package io.defitrack.protocol.aelin

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

class StakingRewardsContract(
    blockchainGateway: BlockchainGateway,
) : EvmContract(
    blockchainGateway, "0xfe757a40f3eda520845b339c698b321663986a4d"
) {

    suspend fun rewardRate(): BigInteger {
        return read(
            "rewardRate", outputs = listOf(
                uint256()
            )
        )[0].value as BigInteger
    }

    fun earned(address: String): ContractCall {
        return createFunction(
            "earned",
            listOf(address.toAddress()),
            listOf(uint256())
        )
    }

    fun getRewardFunction(): ContractCall {
        return createFunction("getReward")
    }
}