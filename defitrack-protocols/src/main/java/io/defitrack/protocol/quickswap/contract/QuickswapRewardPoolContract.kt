package io.defitrack.protocol.quickswap.contract

import arrow.core.nel
import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

class QuickswapRewardPoolContract(
    solidityBasedContractAccessor: BlockchainGateway,
    address: String,
) : ERC20Contract(
    solidityBasedContractAccessor, address
) {

    fun exitFunction(amount: BigInteger): MutableFunction {
        return createFunction(
            "leave",
            amount.toUint256().nel(),
        ).toMutableFunction()
    }

    val periodFinish = constant<BigInteger>("periodFinish", uint256())
    val stakingTokenAddress = constant<String>("stakingToken", address())
    val rewardsTokenAddress = constant<String>("rewardsToken", address())
    val rewardRate = constant<BigInteger>("rewardRate", address())

    fun getRewardFunction(): MutableFunction {
        return createFunction("getReward").toMutableFunction()
    }

    fun earned(user: String): Function {
        return createFunction(
            "earned",
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }
}