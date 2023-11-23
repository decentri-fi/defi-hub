package io.defitrack.protocol.quickswap.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function

class RewardFactoryContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(
    blockchainGateway, address
) {

    suspend fun getRewardPools(): List<String> {
        return readMultiCall(getStakingTokens().map {
            stakingRewardsInfoByStakingToken(it)
        }).filter { it.success }.map { it.data[0].value as String }
    }

    suspend fun getStakingTokens(): List<String> {
        return readMultiCall((0 until 200).map { index ->
            createFunction(
                method = "stakingTokens",
                inputs = listOf(index.toBigInteger().toUint256()),
                outputs = listOf(address())
            )
        }).filter {
            it.success
        }.map {
            it.data[0].value as String
        }
    }

    fun stakingRewardsInfoByStakingToken(stakingToken: String): ContractCall {
        return createFunction(
            "stakingRewardsInfoByStakingToken", listOf(stakingToken.toAddress()), listOf(
                address(),
                uint256(),
                uint256(),
            )
        )
    }
}