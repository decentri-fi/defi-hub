package io.defitrack.protocol.crv.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.ERC20Contract
import java.math.BigInteger

context(BlockchainGateway)
class CurveL2GaugeContract(address: String) : ERC20Contract(address) {


    val chainId = constant<BigInteger>("chain_id", uint256())

    suspend fun lpToken(): String {
        return readSingle(
            "lp_token", address()
        )
    }

    fun getClaimRewardsFunction(): ContractCall {
        return createFunction("claim_rewards")
    }

    fun getClaimableRewardFunction(token: String): (String) -> ContractCall {
        return { user ->
            createFunction(
                "claimable_reward", listOf(user.toAddress(), token.toAddress()), listOf(uint256())
            )
        }
    }


    suspend fun rewardTokens(): List<String> {
        return readMultiCall((0..5).mapNotNull {
            createFunction(
                "reward_tokens", listOf(it.toBigInteger().toUint256()), listOf(address())
            )
        }).filter {
            it.success
        }.map { it.data[0].value as String }
            .filter {
                it != "0x0000000000000000000000000000000000000000"
            }
    }
}