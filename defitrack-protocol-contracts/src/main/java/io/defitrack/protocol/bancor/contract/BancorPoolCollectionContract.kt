package io.defitrack.protocol.bancor.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicArray

context(BlockchainGateway)
class BancorPoolCollectionContract(address: String) : EvmContract(address) {

    suspend fun allTokens(): List<String> {
        val result = read(
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
        val functions = allTokens().map { token ->
            createFunction(
                "poolToken",
                inputs = listOf(token.toAddress()),
                outputs = listOf(TypeUtils.address())
            )
        }

        val results = this.readMultiCall(
            functions
        )
        return results
            .filter { it.success }
            .map { it.data[0].value as String }
    }
}