package io.defitrack.protocol.spark

import arrow.core.nel
import arrow.core.nonEmptyListOf
import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.dynamicArray
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint16
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint128
import io.defitrack.abi.TypeUtils.Companion.uint16
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.abi.TypeUtils.Companion.uint40
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

class PoolContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : EvmContract(blockchainGateway, address) {

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

    suspend fun getReserveData(market: String): ReserveData {
        val result = read(
            "getReserveData",
            market.toAddress().nel(),
            nonEmptyListOf(
                uint256(),
                uint128(),
                uint128(),
                uint128(),
                uint128(),
                uint128(),
                uint40(),
                uint16(),
                address(),
                address(),
                address(),
                address(),
                uint128(),
                uint128(),
                uint128(),
            )
        )
        return ReserveData(
            result[8].value as String,
            result[9].value as String,
            result[10].value as String,
        )
    }

    data class ReserveData(
        val aTokenAddress: String,
        val stableDebtTokenAddress: String,
        val variableDebtTokenAddress: String,
    )
}