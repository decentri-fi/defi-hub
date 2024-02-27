package io.defitrack.protocol.compound.v2.contract

import io.defitrack.abi.TypeUtils.Companion.dynamicArray
import io.defitrack.evm.contract.DeprecatedEvmContract
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Address

context(BlockchainGateway)
open class CompoundComptrollerContract(
    address: String
) : EvmContract(address) {

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