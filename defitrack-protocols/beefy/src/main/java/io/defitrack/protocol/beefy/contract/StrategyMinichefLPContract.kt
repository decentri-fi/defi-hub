package io.defitrack.protocol.beefy.contract

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.BlockchainGateway
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class StrategyMinichefLPContract(
    solidityBasedContractAccessor: BlockchainGateway,
    abi: String,
    address: String
) :
    EvmContract(solidityBasedContractAccessor, abi, address) {

    val chef by lazy {
        var read = readWithAbi(
            "chef",
            outputs = listOf(TypeReference.create(Address::class.java))
        )

        if (read.isEmpty()) {
            read = readWithAbi(
                "minichef",
                outputs = listOf(TypeReference.create(Address::class.java))
            )
        }

        read[0].value as String
    }


    val poolId by lazy {
        readWithAbi(
            "poolId",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }
}