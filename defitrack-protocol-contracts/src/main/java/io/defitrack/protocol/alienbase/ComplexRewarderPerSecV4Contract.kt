package io.defitrack.protocol.alienbase

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract

class ComplexRewarderPerSecV4Contract(
    blockchainGateway: BlockchainGateway,
    address: String
) : EvmContract(
    blockchainGateway, address
) {

    suspend fun rewardToken(): String {
        return readSingle("rewardToken", address())
    }
}