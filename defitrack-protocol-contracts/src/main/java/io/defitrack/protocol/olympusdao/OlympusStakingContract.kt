package io.defitrack.protocol.olympusdao

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.DeprecatedEvmContract

class OlympusStakingContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : DeprecatedEvmContract(blockchainGateway, address) {

    suspend fun gOHM(): String {
        return readSingle("gOHM", address())
    }
}