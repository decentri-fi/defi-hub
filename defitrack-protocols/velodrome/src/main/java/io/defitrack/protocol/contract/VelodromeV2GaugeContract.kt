package io.defitrack.protocol.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract

class VelodromeV2GaugeContract(
    blockchainGateway: BlockchainGateway, address: String
) : ERC20Contract(
    blockchainGateway, "", address
) {

    suspend fun stakedToken(): String {
        return readWithoutAbi(
            "stakingToken",
            inputs = listOf(),
            outputs = listOf(TypeUtils.address())
        )[0].value as String
    }


    suspend fun rewardToken(): String {
        return readWithoutAbi(
            "rewardToken",
            emptyList(),
            listOf(TypeUtils.address())
        )[0].value as String
    }
}