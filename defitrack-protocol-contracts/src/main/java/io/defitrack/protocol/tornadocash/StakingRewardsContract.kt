package io.defitrack.protocol.tornadocash

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.DeprecatedEvmContract
import java.math.BigInteger

class StakingRewardsContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : DeprecatedEvmContract(
    blockchainGateway, address
) {

    suspend fun rewardRate(): BigInteger {
        return readSingle("rewardRate", TypeUtils.uint256())
    }

    suspend fun rewardsToken() : String {
        return readSingle("rewardsToken", TypeUtils.address())
    }

    suspend fun stakingToken() : String{
        return readSingle("stakingToken", TypeUtils.address())
    }

    fun earned(address: String): ContractCall {
        return createFunction(
            "earned",
            listOf(address.toAddress()),
            listOf(TypeUtils.uint256())
        )
    }
}