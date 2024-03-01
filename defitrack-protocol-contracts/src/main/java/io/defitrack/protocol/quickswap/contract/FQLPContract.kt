package io.defitrack.protocol.quickswap.contract

import arrow.core.nel
import io.defitrack.abi.TypeUtils.Companion.dynamicArray
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Address

context(BlockchainGateway)
class FQLPContract(address: String) : EvmContract(address) {

    suspend fun getAllRewardTokens(): List<String> {
        val result = read(
            "getAllRewardTokens", emptyList(), listOf(dynamicArray<Address>())
        )
        return (result.first().value as List<Address>).map { it.value as String }
    }

    fun claimableReward(reward: String): (String) -> ContractCall {
        return { user ->
            createFunction(
                "claimableReward", listOf(user.toAddress(), reward.toAddress()), uint256().nel()
            )
        }
    }

    fun claimAllFn(user: String): ContractCall {
        return createFunction("claimAll", listOf(user.toAddress()), emptyList())
    }
}