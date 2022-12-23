package io.defitrack.protocol.contract

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

class HopSwapContract(
    blockchainGateway: BlockchainGateway,
    abi: String,
    address: String
) : EvmContract(blockchainGateway, abi, address) {

    suspend fun virtualPrice(): BigInteger {
        return readWithAbi("getVirtualPrice")[0].value as BigInteger
    }
}