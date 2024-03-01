package io.defitrack.protocol.baseswap

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

context(BlockchainGateway)
class SmartChefContract(
    address: String
) : EvmContract(address) {

    fun userInfo(address: String): ContractCall {
        return createFunction(
            "userInfo",
            listOf(address.toAddress()),
            listOf(
                uint256(),
                uint256()
            )
        )
    }

    fun withdraw(): ContractCall {
        return createFunction(
            "withdraw",
            listOf(BigInteger.ZERO.toUint256()),
            emptyList()
        )
    }

    fun pendingReward(address: String): ContractCall {
        return createFunction(
            "pendingReward",
            listOf(address.toAddress()),
            listOf(uint256())
        )
    }

    val rewardToken = constant<String>("rewardToken", address())
    val stakedToken = constant<String>("stakedToken", address())
}