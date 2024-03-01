package io.defitrack.protocol.alienbase

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract

context(BlockchainGateway)
class ComplexRewarderPerSecV4Contract(
    address: String
) : EvmContract(address) {

    suspend fun rewardToken(): String {
        return readSingle("rewardToken", address())
    }
}