package io.defitrack.crv

import io.defitrack.ethereumbased.contract.SolidityContract
import io.defitrack.matic.config.PolygonContractAccessor
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address

class CrvPolygonAddressProviderContract(
    polygonContractAccessor: PolygonContractAccessor,
    abi: String,
    address: String
) : SolidityContract(
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