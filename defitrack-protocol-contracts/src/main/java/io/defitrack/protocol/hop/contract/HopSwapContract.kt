package io.defitrack.protocol.hop.contract

import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.DeprecatedEvmContract
import java.math.BigInteger

class HopSwapContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : DeprecatedEvmContract(blockchainGateway, address) {

    suspend fun virtualPrice(): BigInteger {
        return readSingle("getVirtualPrice", uint256())
    }
}