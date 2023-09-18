package io.defitrack.protocol.compound.v2.contract

import io.defitrack.abi.TypeUtils.Companion.dynamicArray
import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.BlockchainGateway
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicArray

class CompoundComptrollerContract(
    ethereumContractAccessor: BlockchainGateway, address: String
) : EvmContract(ethereumContractAccessor, address) {

    suspend fun getMarkets(): List<String> {
        return (read(
            "getAllMarkets",
            inputs = emptyList(),
            outputs = listOf(dynamicArray<Address>())
        )[0].value as List<Address>).map {
            it.value as String
        }
    }
}