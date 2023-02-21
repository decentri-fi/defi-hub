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
    abi: String,
    address: String,
) : ERC20Contract(
    contractAccessor, abi, address
) {
    suspend fun quickBalance(address: String): BigInteger {
        return readWithAbi(
            "QUICKBalance",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                uint256()
            )
        )[0].value as BigInteger
    }

    fun enterFunction(amount: BigInteger): Function {
        return createFunctionWithAbi("enter", listOf(amount.toUint256()), emptyList())
    }

    suspend fun dquickForQuick(amount: BigInteger): BigInteger {
        return readWithoutAbi(
            "dQUICKForQUICK",
            inputs = listOf(amount.toUint256()),
            outputs = listOf(uint256())
        )[0].value as BigInteger
    }
}