package io.defitrack.protocol.quickswap.contract

import arrow.core.nel
import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.ERC20Contract
import org.springframework.cglib.core.Block
import java.math.BigInteger

context(BlockchainGateway)
class QuickswapRewardPoolContract(address: String) : ERC20Contract(address) {

    fun exitFunction(amount: BigInteger): ContractCall {
        return createFunction(
            "leave",
            amount.toUint256().nel(),
        )
    }

    val periodFinish = constant<BigInteger>("periodFinish", uint256())
    val stakingTokenAddress = constant<String>("stakingToken", address())
    val rewardsTokenAddress = constant<String>("rewardsToken", address())
    val rewardRate = constant<BigInteger>("rewardRate", address())

    fun getRewardFunction(): ContractCall {
        return createFunction("getReward")
    }

    fun earned(user: String): ContractCall {
        return createFunction(
            "earned",
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }
}