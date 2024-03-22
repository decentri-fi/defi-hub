package io.defitrack.protocol.hop.contract

import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.DeprecatedEvmContract
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

context(BlockchainGateway)
class HopSwapContract(
    address: String
) : EvmContract(address) {

    suspend fun virtualPrice(): BigInteger {
        return readSingle("getVirtualPrice", uint256())
    }
}