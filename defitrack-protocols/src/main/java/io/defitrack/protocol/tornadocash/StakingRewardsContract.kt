package io.defitrack.protocol.tornadocash

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

class StakingRewardsContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : EvmContract(
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

    fun earned(address: String): Function {
        return createFunction(
            "earned",
            listOf(address.toAddress()),
            listOf(TypeUtils.uint256())
        )
    }
}