package io.defitrack.protocol.contract

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function

class HopStakingReward(
    solidityBasedContractAccessor: BlockchainGateway,
    abi: String,
    address: String,
) : ERC20Contract(
    solidityBasedContractAccessor,
    abi, address
) {

    suspend fun rewardsTokenAddress(): String {
        return readWithAbi(
            "rewardsToken",
            outputs = listOf(TypeReference.create(Address::class.java))
        )[0].value as String
    }

    fun earnedFunction(address: String): Function {
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
        return createFunction(
            "getReward",
            inputs = listOf(),
            outputs = listOf()
        )
    }

    suspend fun stakingTokenAddress(): String {
        return readWithAbi(
            method = "stakingToken",
            outputs = listOf(TypeReference.create(Address::class.java))
        )[0].value as String
    }
}