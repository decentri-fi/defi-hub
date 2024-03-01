package io.defitrack.protocol.olympusdao

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract

context(BlockchainGateway)
class OlympusStakingContract(
    address: String
) : EvmContract(address) {

    suspend fun gOHM(): String {
        return readSingle("gOHM", address())
    }
}