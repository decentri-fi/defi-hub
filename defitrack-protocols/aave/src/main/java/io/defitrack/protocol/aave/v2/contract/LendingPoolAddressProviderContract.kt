package io.defitrack.protocol.aave.v2.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract

class LendingPoolAddressProviderContract(blockchainGateway: BlockchainGateway, abi: String, address: String) :
    EvmContract(
        blockchainGateway, abi, address
    ) {

    suspend fun lendingPoolAddress(): String {
        return readSingle("getLendingPool", TypeUtils.address())
    }
}