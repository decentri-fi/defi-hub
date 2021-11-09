package io.defitrack.protocol.mstable

import io.defitrack.ethereumbased.contract.SolidityBasedContractAccessor
import io.defitrack.ethereumbased.contract.SolidityBasedContractAccessor.Companion.toAddress
import io.defitrack.ethereumbased.contract.SolidityContract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class MStableEthereumSavingsContract(
    ethereumContractAccessor: SolidityBasedContractAccessor,
    abi: String,
    address: String
) : SolidityContract(ethereumContractAccessor, abi, address) {

    val symbol: String by lazy {
        read(
            "symbol"
        )[0].value as String
    }

    val underlying: String by lazy {
        read(
            "underlying"
        )[0].value as String
    }

    val name: String by lazy {
        read(
            "name"
        )[0].value as String
    }

    fun balanceOf(address: String): BigInteger {
        return read(
            "balanceOf",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }

    val decimals: Int by lazy {
        (read(
            "decimals"
        )[0].value as BigInteger).toInt()
    }
}