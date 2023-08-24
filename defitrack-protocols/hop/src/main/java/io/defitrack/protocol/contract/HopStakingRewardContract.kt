package io.defitrack.protocol.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import org.web3j.abi.datatypes.Function

class HopStakingRewardContract(
    solidityBasedContractAccessor: BlockchainGateway,
    abi: String,
    address: String,
) : ERC20Contract(
    solidityBasedContractAccessor,
    abi, address
) {


    suspend fun rewardsTokenAddress(): String {
        return readSingle(
            "rewardsToken",
             address()
        )
    }

    fun earnedFn(address: String): Function {
        return createFunction(
            "earned",
            inputs = listOf(
                address.toAddress()
            ),
            outputs = listOf(
                uint256()
            )
        )
    }

    fun getRewardFn(): Function {
        return createFunction("getReward")
    }

    suspend fun stakingTokenAddress(): String {
        return readSingle(
         "stakingToken",
            address()
        )
    }
}