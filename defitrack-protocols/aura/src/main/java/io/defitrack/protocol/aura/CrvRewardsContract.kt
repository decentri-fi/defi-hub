package io.defitrack.protocol.aura

import io.defitrack.abi.TypeUtils
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class CrvRewardsContract(
    blockchainGateway: BlockchainGateway, address: String
) : ERC20Contract(blockchainGateway, "", address) {

    suspend fun asset(): String {
        return readWithoutAbi(
            method = "asset",
            outputs = listOf(TypeUtils.address())
        )[0].value as String
    }

    val rewardToken = GlobalScope.async(Dispatchers.Unconfined, start = CoroutineStart.LAZY) {
        readWithoutAbi(
            method = "rewardToken",
            outputs = listOf(TypeUtils.address())
        )[0].value as String
    }
}