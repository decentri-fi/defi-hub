package io.defitrack.protocol.aura

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract

context(BlockchainGateway)
class CrvRewardsContract(address: String) : ERC20Contract(address) {

    val asset = constant<String>("asset", address())
    val rewardToken = constant<String>("rewardToken", address())
}