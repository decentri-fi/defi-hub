package io.defitrack.protocol.beefy.contract

import io.defitrack.ethereumbased.contract.EvmContract
import io.defitrack.ethereumbased.contract.EvmContractAccessor
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class StrategyMinichefLPContract(
    solidityBasedContractAccessor: EvmContractAccessor,
    abi: String,
    address: String
) :
    EvmContract(solidityBasedContractAccessor, abi, address) {

    val chef by lazy {
        var read = read(
            "chef",
            outputs = listOf(TypeReference.create(Address::class.java))
        )

        if (read.isEmpty()) {
            read = read(
                "minichef",
                outputs = listOf(TypeReference.create(Address::class.java))
            )
        }

        read[0].value as String
    }


    val poolId by lazy {
        read(
            "poolId",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }
}