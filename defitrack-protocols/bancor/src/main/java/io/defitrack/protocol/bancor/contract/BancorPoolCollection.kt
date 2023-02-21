package io.defitrack.protocol.bancor.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.multicall.MultiCallElement
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicArray

class BancorPoolCollection(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(
    blockchainGateway, "", address
) {

    suspend fun allTokens(): List<String> {
        val result = readWithoutAbi(
            "pools",
            outputs = listOf(
                object : TypeReference<DynamicArray<Address>>() {}
            )
        )
        return (result[0].value as List<Address>).map {
            it.value
        }
    }

    suspend fun allPools(): List<String> {
        val multicalls = allTokens().map { token ->
            MultiCallElement(
                createFunction(
                    "poolToken",
                    inputs = listOf(token.toAddress()),
                    outputs = listOf(
                        TypeUtils.address(),
                    )
                ),
                this.address
            )
        }

        val results = this.blockchainGateway.readMultiCall(
            multicalls
        )
        return results.map { it[0].value as String }
    }

}