package io.defitrack.protocol.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

class HopSwapContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : EvmContract(blockchainGateway, "", address) {

    suspend fun virtualPrice(): BigInteger {
        return readSingle("getVirtualPrice", TypeUtils.address())
    }
}