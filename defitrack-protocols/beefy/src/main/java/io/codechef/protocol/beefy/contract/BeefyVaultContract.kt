package io.defitrack.protocol.beefy.contract

import io.defitrack.ethereumbased.contract.ERC20Contract
import io.defitrack.ethereumbased.contract.SolidityBasedContractAccessor
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class BeefyVaultContract(
    solidityBasedContractAccessor: SolidityBasedContractAccessor,
    abi: String,
    address: String,
    val vaultId: String,
) :
    ERC20Contract(
        solidityBasedContractAccessor, abi, address
    ) {

    val balance by lazy {
        read(
            "balance",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }

    val getPricePerFullShare by lazy {
        read(
            "getPricePerFullShare",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }

    val strategy by lazy {
        read(
            "strategy",
            outputs = listOf(TypeReference.create(Address::class.java))
        )[0].value as String
    }

    val want: String by lazy {
        var read = read(
            "want",
            inputs = emptyList(),
            outputs = listOf(
                TypeReference.create(Address::class.java)
            )
        )
        if (read.isEmpty()) {
            read = read(
                "wmatic",
                inputs = emptyList(),
                outputs = listOf(
                    TypeReference.create(Address::class.java)
                )
            )
        }
        if (read.isEmpty()) {
            read = read(
                "token",
                inputs = emptyList(),
                outputs = listOf(
                    TypeReference.create(Address::class.java)
                )
            )
        }
        if (read.isEmpty()) {
            read = read(
                "wbnb",
                inputs = emptyList(),
                outputs = listOf(
                    TypeReference.create(Address::class.java)
                )
            )
        }
            read[0].value as String
    }
}