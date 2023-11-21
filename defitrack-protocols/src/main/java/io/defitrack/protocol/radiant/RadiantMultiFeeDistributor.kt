package io.defitrack.protocol.radiant

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.Function

class RadiantMultiFeeDistributor(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(blockchainGateway, address) {

    val stakingToken = constant<String>("stakingToken", TypeUtils.address())

    fun getRewardFn(tokens: List<String>): MutableFunction {
        return createFunction(
            "getReward",
            tokens.map { it.toAddress() },
            emptyList()
        ).toMutableFunction()
    }

    suspend fun rewardTokens(): List<String> {
        return readMultiCall(
            (0 until 10).map {
                createFunction(
                    "rewardTokens",
                    listOf(it.toBigInteger().toUint256()),
                    listOf(TypeUtils.address())
                )
            }
        ).filter {
            it.success
        }.map { it.data[0].value as String }
            .filter {
                it != "0x0000000000000000000000000000000000000000"
            }
    }

    fun getClaimableRewardFn(user: String): Function {
        return createFunction(
            "claimableRewards",
            listOf(user.toAddress()),
            listOf(object : TypeReference<DynamicArray<RadiantMultiFeeReward>>() {})
        )
    }
}