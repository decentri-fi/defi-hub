package io.defitrack.protocol.quickswap.contract

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGateway.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway.Companion.toUint256
import io.defitrack.evm.contract.ERC20Contract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class DQuickContract(
    contractAccessor: BlockchainGateway,
    abi: String,
    address: String,
) : ERC20Contract(
    contractAccessor, abi, address
) {
    fun quickBalance(address: String): BigInteger {
        return read(
            "QUICKBalance",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }

    fun enterFunction(amount: BigInteger): Function {
        return createFunction("enter", listOf(amount.toUint256()), emptyList())
    }

    fun dquickForQuick(amount: BigInteger): BigInteger {
        return read(
            "dQUICKForQuick",
            inputs = listOf(amount.toUint256()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }
}