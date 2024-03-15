package io.defitrack.protocol.alienbase

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.DeprecatedEvmContract

class ComplexRewarderPerSecV4Contract(
    blockchainGateway: BlockchainGateway,
    address: String
) : DeprecatedEvmContract(
    blockchainGateway, address
) {

    suspend fun rewardToken(): String {
        return readSingle("rewardToken", address())
    }
}