package io.defitrack.protocol.contract

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.BlockchainGateway
import java.math.BigInteger

class HopSwapContract(
    blockchainGateway: BlockchainGateway,
    abi: String,
    address: String
) : EvmContract(blockchainGateway, abi, address) {

    val virtualPrice by lazy {
        read("getVirtualPrice")[0].value as BigInteger
    }
}