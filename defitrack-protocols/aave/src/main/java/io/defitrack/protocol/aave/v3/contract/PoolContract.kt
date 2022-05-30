package io.defitrack.protocol.aave.v3.contract

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicArray

class PoolContract(
    blockchainGateway: BlockchainGateway,
    abi: String,
    address: String
) : EvmContract(blockchainGateway, abi, address) {

    val reservesList by lazy {
        (read("getReservesList", emptyList(),  listOf(
            object : TypeReference<DynamicArray<Address>>() {}
        ))[0].value as List<Address>).map {
            it.value as String
        }
    }
}