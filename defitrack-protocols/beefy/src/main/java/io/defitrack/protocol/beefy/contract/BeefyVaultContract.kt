package io.defitrack.protocol.beefy.contract

import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGateway.Companion.toUint256
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class BeefyVaultContract(
    solidityBasedContractAccessor: BlockchainGateway,
    abi: String,
    address: String,
    val vaultId: String,
) :
    ERC20Contract(
        solidityBasedContractAccessor, abi, address
    ) {

    val balance by lazy {
        readWithAbi(
            "balance",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }

    fun depositAllFunction(): Function {
        return createFunctionWithAbi("depositAll", emptyList(), emptyList())
    }

    fun depositFunction(amount: BigInteger): Function {
        return createFunctionWithAbi("deposit", listOf(amount.toUint256()), emptyList())
    }

    val getPricePerFullShare by lazy {
        readWithAbi(
            "getPricePerFullShare",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }

    val strategy by lazy {
        readWithAbi(
            "strategy",
            outputs = listOf(TypeReference.create(Address::class.java))
        )[0].value as String
    }

    val want: String by lazy {
        var read = readWithAbi(
            "want",
            inputs = emptyList(),
            outputs = listOf(
                TypeReference.create(Address::class.java)
            )
        )
        if (read.isEmpty()) {
            read = readWithAbi(
                "wmatic",
                inputs = emptyList(),
                outputs = listOf(
                    TypeReference.create(Address::class.java)
                )
            )
        }
        if (read.isEmpty()) {
            read = readWithAbi(
                "token",
                inputs = emptyList(),
                outputs = listOf(
                    TypeReference.create(Address::class.java)
                )
            )
        }
        if (read.isEmpty()) {
            read = readWithAbi(
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