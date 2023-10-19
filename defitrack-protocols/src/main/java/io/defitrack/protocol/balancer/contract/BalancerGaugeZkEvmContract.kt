package io.defitrack.protocol.balancer.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

class BalancerGaugeZkEvmContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : BalancerGaugeContract(blockchainGateway, address) {

    fun integrateFraction(user: String): Function {
        return createFunction(
            "integrate_fraction",
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }

    override fun getClaimableRewardFunction(address: String, token: String): Function {
        return Function(
            "claimable_reward",
            listOf(
                address.toAddress(),
                token.toAddress()
            ),
            listOf(uint256())
        )
    }

    val lpToken = constant<String>("lp_token", address())
    val rewardCount = constant<BigInteger>("reward_count", uint256())

    suspend fun rewardTokens(): List<String> {
        return readMultiCall(
            (0 until rewardCount.await().toInt()).map {
                createFunction(
                    "reward_tokens",
                    listOf(it.toBigInteger().toUint256()),
                    listOf(address())
                )
            }
        ).map {
            it.data[0].value as String
        }
    }
}