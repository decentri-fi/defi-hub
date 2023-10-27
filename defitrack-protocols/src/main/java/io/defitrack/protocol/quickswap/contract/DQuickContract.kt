package io.defitrack.protocol.quickswap.contract

import io.defitrack.abi.TypeUtils.Companion.toAddress
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


    fun exitFunction(amount: BigInteger): Function {
        return createFunction(
            "leave",
            listOf(amount.toUint256()),
            listOf()
        )
    }

    fun enterFunction(amount: BigInteger): Function {
        return createFunction("enter", listOf(amount.toUint256()), emptyList())
    }

    suspend fun dquickForQuick(amount: BigInteger): BigInteger {
        return read(
            "dQUICKForQUICK",
            inputs = listOf(amount.toUint256()),
            outputs = listOf(uint256())
        )[0].value as BigInteger
    }
}