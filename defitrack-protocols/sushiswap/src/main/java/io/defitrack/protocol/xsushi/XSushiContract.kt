package io.defitrack.protocol.xsushi

import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

class XSushiContract(blockchainGateway: BlockchainGateway, abi: String, address: String) :
    EvmContract(blockchainGateway, abi, address) {

    suspend fun totalSupply(): BigInteger {
        return readWithoutAbi("totalSupply", emptyList(), listOf(
            uint256()
        ))[0].value as BigInteger
    }
}