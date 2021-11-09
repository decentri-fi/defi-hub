package io.defitrack.protocol.compound

import io.defitrack.ethereum.config.EthereumContractAccessor
import io.defitrack.ethereumbased.contract.EvmContract
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicArray

class CompoundComptrollerContract(
    ethereumContractAccessor: EthereumContractAccessor,
    abi: String, address: String
) : EvmContract(
    ethereumContractAccessor, abi, address
) {

    fun getMarkets(): List<String> {
        return (read(
            "getAllMarkets",
            inputs = emptyList(),
            outputs = listOf(
                object : org.web3j.abi.TypeReference<DynamicArray<Address>>() {}
            )
        )[0].value as List<Address>).map {
            it.value as String
        }
    }
}