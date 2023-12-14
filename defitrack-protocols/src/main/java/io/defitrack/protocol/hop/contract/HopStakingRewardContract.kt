package io.defitrack.protocol.hop.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.ERC20Contract

class HopStakingRewardContract(
    solidityBasedContractAccessor: BlockchainGateway,
    address: String,
) : ERC20Contract(
    solidityBasedContractAccessor,
    address
) {


    suspend fun rewardsTokenAddress(): String {
        return readSingle(
            "rewardsToken",
            address()
        )
    }

    fun earnedFn(address: String): ContractCall {
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

    fun getRewardFn(): ContractCall {
        return createFunction("getReward")
    }

    suspend fun stakingTokenAddress(): String {
        return readSingle("stakingToken", address())
    }
}