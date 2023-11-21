package io.defitrack.protocol.crv.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function

class CurveL2GaugeContract(
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

    fun getClaimRewardsFunction(): ContractCall {
        return createFunction(
            "claim_rewards"
        ).toContractCall()
    }

    fun getClaimableRewardFunction(token: String): (String) -> Function {
        return { user ->
            createFunction(
                "claimable_reward",
                listOf(user.toAddress(), token.toAddress()),
                listOf(uint256())
            )
        }
    }


    suspend fun rewardTokens(): List<String> {
        return readMultiCall((0..5).mapNotNull {
            createFunction(
                "reward_tokens",
                listOf(it.toBigInteger().toUint256()),
                listOf(address())
            )
        }).filter {
            it.success
        }.map { it.data[0].value as String }
    }
}