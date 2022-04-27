package io.defitrack.protocol.contract

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.BlockchainGateway
import java.math.BigInteger

class HopLpTokenContract(
    blockchainGateway: BlockchainGateway,
    abi: String,
    address: String
) : EvmContract(blockchainGateway, abi, address) {

    val name by lazy {
        read("name")[0].value as String
    }

    val symbol by lazy {
        read("symbol")[0].value as String
    }

    val swap by lazy {
        read("swap")[0].value as String
    }

    val decimals by lazy {
        (read("decimals")[0].value as BigInteger).toInt()
    }

    val totalSupply by lazy {
        read("totalSupply")[0].value as BigInteger
    }
}