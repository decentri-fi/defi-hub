package io.defitrack.protocol.baseswap

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

class SmartChefContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(blockchainGateway, address) {

    fun userInfo(address: String): Function {
        return createFunction(
            "userInfo",
            listOf(
                address.toAddress()
            ),
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
        ).toContractCall()
    }

    fun pendingReward(address: String): Function {
        return createFunction(
            "pendingReward",
            listOf(
                address.toAddress()
            ),
            listOf(
                uint256()
            )
        )
    }

    val rewardToken = constant<String>("rewardToken", address())
    val stakedToken = constant<String>("stakedToken", address())
}