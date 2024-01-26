package io.defitrack.protocol.morpho

import arrow.core.nel
import io.defitrack.abi.TypeUtils.Companion.dynamicArray
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Address

class MorphoContract(
    blockchainGateway: BlockchainGateway, address: String
): EvmContract(
    blockchainGateway, address
) {

    suspend fun marketsCreated(): List<String> {
        val result =  read("marketsCreated",
            emptyList(),
            dynamicArray<Address>().nel()
        )
        return (result.first().value as List<Address>).map { it.value as String }
    }

    fun collateralBalance(underlying: String): (String) -> ContractCall {
        return  { user: String ->
            createFunction("supplyBalance",
            listOf(
                underlying.toAddress(),
                user.toAddress()
            ) , uint256().nel())
        }
    }
}