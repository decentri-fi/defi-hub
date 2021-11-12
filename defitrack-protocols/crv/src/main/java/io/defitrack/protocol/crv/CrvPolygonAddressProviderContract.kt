package io.defitrack.protocol.crv

import io.defitrack.ethereumbased.contract.EvmContract
import io.defitrack.ethereumbased.contract.EvmContractAccessor
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address

class CrvPolygonAddressProviderContract(
    polygonContractAccessor: EvmContractAccessor,
    abi: String,
    address: String
) : EvmContract(
    polygonContractAccessor,
    abi,
    address
) {

    fun getRegistry(): String {
        return read(
            "get_registry",
            outputs = listOf(
                TypeReference.create(Address::class.java)
            )
        )[0].value as String
    }
}