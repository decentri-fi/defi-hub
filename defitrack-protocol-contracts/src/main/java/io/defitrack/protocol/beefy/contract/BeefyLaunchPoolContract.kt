package io.defitrack.protocol.beefy.contract

import arrow.core.nel
import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

context(BlockchainGateway)
class BeefyLaunchPoolContract(address: String) : EvmContract(address) {

    val stakedToken = constant<String>("stakedToken", TypeUtils.address())

    fun earned(user: String): ContractCall {
        return createFunction(
            "earned",
            user.toAddress().nel(),
            uint256().nel()
        )
    }

    fun getRewardfn(): ContractCall {
        return createFunction("getReward")
    }

    fun exitPosition(amount: BigInteger): ContractCall {
        return if (amount == BigInteger.ZERO) {
            createFunction("exit")
        } else {
            createFunction(
                "withdraw",
                amount.toUint256().nel()
            )
        }
    }
}