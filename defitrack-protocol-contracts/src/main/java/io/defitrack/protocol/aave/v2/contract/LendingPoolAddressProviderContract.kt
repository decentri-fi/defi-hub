package io.defitrack.protocol.aave.v2.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.DeprecatedEvmContract

class LendingPoolAddressProviderContract(blockchainGateway: BlockchainGateway, address: String) :
    DeprecatedEvmContract(
        blockchainGateway, address
    ) {

    suspend fun lendingPoolAddress(): String {
        return readSingle("getLendingPool", address())
    }
}