package io.defitrack.protocol.compound

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.BlockchainGateway
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicArray

class IronBankComptrollerContract(
    blockchainGateway: BlockchainGateway,
    abi: String, address: String
) : EvmContract(
    blockchainGateway, abi, address
) {

    suspend fun getMarkets(): List<String> {
        return (readWithAbi(
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