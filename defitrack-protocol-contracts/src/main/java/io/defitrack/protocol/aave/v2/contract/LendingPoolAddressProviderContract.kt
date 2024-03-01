package io.defitrack.protocol.aave.v2.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract

context(BlockchainGateway)
class LendingPoolAddressProviderContract(address: String) :
    EvmContract(
        address
    ) {

    suspend fun lendingPoolAddress(): String {
        return readSingle("getLendingPool", address())
    }
}