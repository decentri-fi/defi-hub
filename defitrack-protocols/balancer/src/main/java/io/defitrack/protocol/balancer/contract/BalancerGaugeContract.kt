package io.defitrack.protocol.balancer.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.evm.contract.multicall.MultiCallElement
import io.defitrack.token.FungibleToken
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

open class BalancerGaugeContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : ERC20Contract(blockchainGateway, "", address) {


    fun exitPosition(amount: BigInteger): Function {
        return createFunction(
            "withdraw",
            listOf(amount.toUint256()),
            listOf()
        )
    }

    open fun getClaimableRewardFunction(address: String, token: String): Function {
        return createFunction(
            "claimable_reward_write",
            listOf(
                address.toAddress(),
                token.toAddress()
            ),
            listOf(uint256())
        )
    }

    fun getClaimRewardsFunction(): Function {
        return createFunction(
            "claim_rewards",
            emptyList(),
            emptyList()
        )
    }

    suspend fun getBalances(user: String, rewardTokens: List<FungibleToken>): List<BalancerGaugeBalance> {
        return readMultiCall(
            rewardTokens.map { token ->
                getClaimableRewardFunction(user, token.address)
            }
        ).mapIndexed { index, retVal ->
            val token = rewardTokens[index]
            BalancerGaugeBalance(
                token = token,
                retVal.data[0].value as BigInteger
            )
        }
    }

    class BalancerGaugeBalance(
        val token: FungibleToken,
        val balance: BigInteger
    )

    suspend fun getRewardTokens(): List<String> {
        return (0..3).mapNotNull {
            try {
                val rewardToken = getRewardToken(it)
                if (rewardToken != "0x0000000000000000000000000000000000000000") {
                    rewardToken
                } else {
                    null
                }
            } catch (ex: Exception) {
                null
            }
        }
    }


    suspend fun getRewardToken(index: Int): String {
        return readWithoutAbi(
            "reward_tokens",
            listOf(index.toBigInteger().toUint256()),
            listOf(address())
        )[0].value as String
    }

    suspend fun getStakedToken(): String {
        return readSingle("lp_token", address())
    }
}