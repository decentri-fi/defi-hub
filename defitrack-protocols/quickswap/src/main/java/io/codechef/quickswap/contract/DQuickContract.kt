package io.defitrack.quickswap.contract

import io.defitrack.ethereumbased.contract.SolidityBasedContractAccessor
import io.defitrack.ethereumbased.contract.SolidityBasedContractAccessor.Companion.toAddress
import io.defitrack.ethereumbased.contract.SolidityBasedContractAccessor.Companion.toUint256
import io.defitrack.ethereumbased.contract.SolidityContract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class DQuickContract(
    contractAccessor: SolidityBasedContractAccessor,
    abi: String,
    address: String,
) : SolidityContract(
    contractAccessor, abi, address
) {
    fun balanceOf(address: String): BigInteger {
        return read(
            "balanceOf",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }

    fun quickBalance(address: String): BigInteger {
        return read(
            "QUICKBalance",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
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