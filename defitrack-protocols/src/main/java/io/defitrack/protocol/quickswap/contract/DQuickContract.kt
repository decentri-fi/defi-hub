package io.defitrack.protocol.quickswap.contract

import arrow.core.nel
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

class DQuickContract(
    contractAccessor: BlockchainGateway,
    address: String,
) : ERC20Contract(
    contractAccessor, address
) {


    fun exitFunction(amount: BigInteger): MutableFunction {
        return createFunction(
            "leave",
            listOf(amount.toUint256()),
            listOf()
        ).toMutableFunction()
    }

    fun enterFunction(amount: BigInteger): MutableFunction {
        return createFunction("enter", amount.toUint256().nel()).toMutableFunction()
    }

    suspend fun dquickForQuick(amount: BigInteger): BigInteger {
        return read(
            "dQUICKForQUICK",
            inputs = listOf(amount.toUint256()),
            outputs = listOf(uint256())
        )[0].value as BigInteger
    }
}