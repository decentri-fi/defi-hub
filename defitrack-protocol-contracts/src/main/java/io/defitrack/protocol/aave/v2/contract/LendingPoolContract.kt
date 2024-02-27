package io.defitrack.protocol.aave.v2.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.dynamicArray
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint16
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint128
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.abi.TypeUtils.Companion.uint40
import io.defitrack.abi.TypeUtils.Companion.uint8
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.DeprecatedEvmContract
import org.web3j.abi.datatypes.Address
import java.math.BigInteger

class LendingPoolContract(blockchainGateway: BlockchainGateway, address: String) :
    DeprecatedEvmContract(
        blockchainGateway, address
    ) {
    suspend fun getReservesList(): List<String> {
        return (read(
            "getReservesList",
            emptyList(),
            listOf(dynamicArray<Address>(false))
        )[0].value as List<Address>).map { it.value as String }
    }

    suspend fun getReserveData(address: String): ReserveData {
        val results = read(
            "getReserveData",
            listOf(address.toAddress()),
            listOf(
                uint256(), //actually a struct..
                uint128(), //liqindex
                uint128(), //varborrowIndex,
                uint128(), //currliquiIndex,
                uint128(), //currvarborrowrate,
                uint128(), //currstableborrowrate,
                uint40(), //lastupdateTimestamp,
                TypeUtils.address(), //aTokenAddress,
                TypeUtils.address(), //stableDebtTokenAddress,
                TypeUtils.address(), //variableDebtTokenAddress,
                TypeUtils.address(), //interestRateStrategyAddress,
                uint8(), // id
            )
        )
        return ReserveData(
            results[7].value as String,
            results[8].value as String,
            results[9].value as String,
        )
    }

    fun depositFunction(asset: String, amount: BigInteger): ContractCall {
        return createFunction(
            "deposit",
            listOf(
                asset.toAddress(),
                amount.toUint256(),
                "0000000000000000000000000000000000000000".toAddress(),
                BigInteger.ZERO.toUint16()
            ),
            emptyList()
        )
    }

    data class ReserveData(
        val aTokenAddress: String,
        val stableDebtTokenAddress: String,
        val variableDebtTokenAddress: String,
    )

}