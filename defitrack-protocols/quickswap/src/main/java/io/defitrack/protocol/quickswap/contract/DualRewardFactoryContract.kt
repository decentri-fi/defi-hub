package io.defitrack.protocol.quickswap.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract

class DualRewardFactoryContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(
    blockchainGateway, "", address
) {

    suspend fun getStakingTokens(): List<String> {
        return (0 until 50).mapNotNull { index ->
            val retVal = readWithoutAbi(
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
        return readWithoutAbi(
            "stakingRewardsInfoByStakingToken",
            listOf(stakingToken.toAddress()),
            listOf(
                address(),
                address(),
                address(),
                uint256(),
                uint256(),
                uint256(),
            )
        )[0].value as String
    }

}