package io.defitrack.protocol.balancer.contract

import arrow.core.nonEmptyListOf
import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.ERC20Contract
import java.math.BigInteger

context(BlockchainGateway)
open class BalancerGaugeContract(address: String) : ERC20Contract(address) {

    val workingSupply = constant<BigInteger>("working_supply", uint256())

    fun workingBalance(user: String): ContractCall {
        return createFunction(
            "working_balances",
            nonEmptyListOf(user.toAddress()),
            nonEmptyListOf(uint256())
        )
    }

    fun exitPosition(amount: BigInteger): ContractCall {
        return createFunction(
            "withdraw",
            nonEmptyListOf(amount.toUint256()),
            listOf()
        )
    }

    open fun getClaimableRewardFunction(token: String): (String) -> ContractCall {
        return { user: String ->
            createFunction(
                "claimable_reward_write",
                listOf(
                    user.toAddress(),
                    token.toAddress()
                ),
                listOf(uint256())
            )
        }
    }

    fun getClaimRewardsFunction(): ContractCall {
        return createFunction(
            "claim_rewards",
            emptyList(),
            emptyList()
        )
    }

    suspend fun getRewardTokens(): List<String> {
        return readMultiCall(
            (0 until 5).map {
                getRewardToken(it)
            }
        ).filter {
            it.success
        }.map {
            it.data[0].value as String
        }.filter {
            it != "0x0000000000000000000000000000000000000000"
        }
    }


    fun getRewardToken(index: Int): ContractCall {
        return createFunction(
            "reward_tokens",
            listOf(index.toBigInteger().toUint256()),
            listOf(address())
        )
    }

    suspend fun getStakedToken(): String {
        return readSingle("lp_token", address())
    }
}