package io.defitrack.pool.contract

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGateway.Companion.toAddress
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class LPTokenContract(
    solidityBasedContractAccessor: BlockchainGateway,
    abi: String,
    address: String
) :
    EvmContract(solidityBasedContractAccessor, abi, address) {

    val name by lazy {
        readWithAbi("name")[0].value as String
    }

    val symbol by lazy {
        readWithAbi("symbol")[0].value as String
    }

    fun balanceOf(address: String): BigInteger {
        return readWithAbi(
            "balanceOf",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }

    val decimals by lazy {
        (readWithAbi("decimals")[0].value as BigInteger).toInt()
    }

    val token0 by lazy {
        readWithAbi("token0")[0].value as String
    }

    val token1 by lazy {
        readWithAbi("token1")[0].value as String
    }

    val totalSupply by lazy {
        readWithAbi("totalSupply")[0].value as BigInteger
    }
}