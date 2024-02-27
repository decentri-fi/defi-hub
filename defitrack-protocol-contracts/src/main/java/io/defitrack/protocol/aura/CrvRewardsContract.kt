package io.defitrack.protocol.aura

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract

context(BlockchainGateway)
class CrvRewardsContract(address: String) : ERC20Contract(address) {

    //todo, don't fetch

    suspend fun asset(): String {
        return readSingle("asset", address())
    }

    val rewardToken = lazyAsync {
        readSingle<String>("rewardToken", address())
    }
}