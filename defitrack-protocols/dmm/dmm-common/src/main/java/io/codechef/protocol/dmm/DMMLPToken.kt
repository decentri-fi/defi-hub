package io.defitrack.protocol.dmm

import io.defitrack.ethereumbased.contract.SolidityBasedContractAccessor
import io.defitrack.ethereumbased.contract.SolidityBasedContractAccessor.Companion.toAddress
import io.defitrack.ethereumbased.contract.SolidityContract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class DMMLPToken(
    solidityBasedContractAccessor: SolidityBasedContractAccessor,
    abi: String,
    address: String
) :
    SolidityContract(solidityBasedContractAccessor, abi, address) {

    val name by lazy {
        read("name")[0].value as String
    }

    val symbol by lazy {
        read("symbol")[0].value as String
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

    val decimals by lazy {
        (read("decimals")[0].value as BigInteger).toInt()
    }

    val token0 by lazy {
        read("token0")[0].value as String
    }

    val token1 by lazy {
        read("token1")[0].value as String
    }

    val totalSupply by lazy {
        read("totalSupply")[0].value as BigInteger
    }
}