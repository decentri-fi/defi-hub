package io.defitrack.protocol.aura

import io.defitrack.abi.TypeUtils
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class CrvRewardsContract(
    blockchainGateway: BlockchainGateway, address: String
) : ERC20Contract(blockchainGateway, address) {

    suspend fun asset(): String {
        return readSingle("asset", TypeUtils.address())
    }

    val rewardToken = lazyAsync {
        readSingle<String>("rewardToken", TypeUtils.address())
    }
}