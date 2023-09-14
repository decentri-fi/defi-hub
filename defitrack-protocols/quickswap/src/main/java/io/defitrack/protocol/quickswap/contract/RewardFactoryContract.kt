package io.defitrack.protocol.quickswap.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract

class RewardFactoryContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(
    blockchainGateway, "", address
) {

    suspend fun getStakingTokens(): List<String> {
        return (0 until 200).mapNotNull { index ->
            val retVal = read(
                method = "stakingTokens",
                inputs = listOf(index.toBigInteger().toUint256()),
                outputs = listOf(address())
            )
            return@mapNotNull if (retVal.isEmpty()) {
                null
            } else {
                retVal[0].value as String
            }
        }
    }

    suspend fun stakingRewardsInfoByStakingToken(stakingToken: String): String {
        return read(
            "stakingRewardsInfoByStakingToken",
            listOf(stakingToken.toAddress()),
            listOf(
                address(),
                uint256(),
                uint256(),
            )
        )[0].value as String
    }

}