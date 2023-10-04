package io.defitrack.protocol.crv.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function

class CurveGaugeContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : EvmContract(
    blockchainGateway, address

) {

    suspend fun lpToken(): String {
        return readSingle(
            "lp_token", address()
        )
    }

    fun getClaimRewardsFunction(): Function {
        return createFunction(
            "claim_rewards"
        )
    }

    fun getClaimableRewardFunction(address: String, token: String): Function {
        return createFunction(
            "claimable_reward",
            listOf(address.toAddress(), token.toAddress()),
            listOf(uint256())
        )
    }

    suspend fun rewardTokens(): List<String> {
        return (0..3).mapNotNull {
            try {
                read(
                    "reward_tokens",
                    listOf(it.toBigInteger().toUint256()),
                    listOf(address())
                )[0].value as String
            } catch (ex: Exception) {
                null
            }
        }
    }

}