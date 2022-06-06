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
        readWithAbi("name")[0].value as String
    }

    val symbol by lazy {
        readWithAbi("symbol")[0].value as String
    }

    val swap by lazy {
        readWithAbi("swap")[0].value as String
    }

    val decimals by lazy {
        (readWithAbi("decimals")[0].value as BigInteger).toInt()
    }

    val totalSupply by lazy {
        readWithAbi("totalSupply")[0].value as BigInteger
    }
}