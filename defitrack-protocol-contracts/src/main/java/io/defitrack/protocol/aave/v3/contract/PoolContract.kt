package io.defitrack.protocol.aave.v3.contract

import io.defitrack.abi.TypeUtils.Companion.dynamicArray
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint16
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.DeprecatedEvmContract
import org.web3j.abi.datatypes.Address
import java.math.BigInteger

class PoolContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : DeprecatedEvmContract(blockchainGateway, address) {

    fun getSupplyFunction(asset: String, amount: BigInteger, onBehalfOf: String): ContractCall {
        return createFunction(
            "supply",
            listOf(
                asset.toAddress(),
                amount.toUint256(),
                onBehalfOf.toAddress(),
                BigInteger.ZERO.toUint16()
            )
        )
    }

    suspend fun reservesList(): List<String> {
        return (read(
            "getReservesList", emptyList(), listOf(
                dynamicArray<Address>()
            )
        )[0].value as List<Address>).map {
            it.value as String
        }
    }
}