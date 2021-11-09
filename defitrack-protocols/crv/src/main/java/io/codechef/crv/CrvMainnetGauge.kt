package io.defitrack.crv

import io.defitrack.ethereumbased.contract.SolidityBasedContractAccessor
import io.defitrack.ethereumbased.contract.SolidityBasedContractAccessor.Companion.toAddress
import io.defitrack.ethereumbased.contract.SolidityContract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class CrvMainnetGauge(
    ethereumContractAccessor: SolidityBasedContractAccessor,
    abi: String,
    address: String,
) : SolidityContract(ethereumContractAccessor, abi, address) {

    fun balanceOf(address: String): BigInteger {
        return read(
            "balanceOf",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }

    val name: String by lazy {
        read(
            "name",
            inputs = emptyList(),
            outputs = listOf(
                TypeReference.create(Utf8String::class.java)
            )
        )[0].value as String
    }

    val symbol: String by lazy {
        read(
            "symbol",
            inputs = emptyList(),
            outputs = listOf(
                TypeReference.create(Utf8String::class.java)
            )
        )[0].value as String
    }

    val decimals: BigInteger by lazy {
        read(
            "decimals",
            inputs = emptyList(),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }

    val lpToken: String by lazy {
        read(
            "lp_token",
            inputs = emptyList(),
            outputs = listOf(
                TypeReference.create(Address::class.java)
            )
        )[0].value as String
    }
}