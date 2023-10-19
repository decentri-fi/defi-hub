package io.defitrack.protocol.balancer.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.token.FungibleToken
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

open class BalancerGaugeContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : ERC20Contract(blockchainGateway, address) {


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

    fun getClaimRewardsFunction(): ContractCall {
        return createFunction(
            "claim_rewards",
            emptyList(),
            emptyList()
        ).toContractCall()
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


    suspend fun getRewardToken(index: Int): Function {
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