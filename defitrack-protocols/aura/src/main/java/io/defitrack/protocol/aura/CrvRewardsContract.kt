package io.defitrack.protocol.aura

import io.defitrack.abi.TypeUtils
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract

class CrvRewardsContract(
    blockchainGateway: BlockchainGateway, address: String
) : ERC20Contract(blockchainGateway, "", address) {

    suspend fun asset(): String {
        return readWithoutAbi(
            method = "asset",
            outputs = listOf(TypeUtils.address())
        )[0].value as String
    }

    suspend fun rewardToken(): String {
        return readWithoutAbi(
            method = "rewardToken",
            outputs = listOf(TypeUtils.address())
        )[0].value as String
    }
}