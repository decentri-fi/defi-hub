package io.defitrack.protocol.ironbank

import io.defitrack.evm.contract.DeprecatedEvmContract
import io.defitrack.evm.contract.BlockchainGateway
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicArray

class IronBankComptrollerContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : DeprecatedEvmContract(
    blockchainGateway, address
) {

    suspend fun getMarkets(): List<String> {
        return (read(
            "getAllMarkets",
            inputs = emptyList(),
            outputs = listOf(
                object : TypeReference<DynamicArray<Address>>() {}
            )
        )[0].value as List<Address>).map {
            it.value as String
        }
    }
}