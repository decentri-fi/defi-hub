package io.defitrack.protocol.yearn

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.BlockchainGateway
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class YearnEthereumVaultV2Contract(
    ethereumContractAccessor: BlockchainGateway,
    abi: String,
    address: String
) : EvmContract(ethereumContractAccessor, abi, address) {
    val name: String by lazy {
        read(
            "name",
            inputs = emptyList(),
            outputs = listOf(
                TypeReference.create(Utf8String::class.java)
            )
        )[0].value as String
    }

    val stakedToken: String by lazy {
        read(
            "token",
            inputs = emptyList(),
            outputs = listOf(
                TypeReference.create(Address::class.java)
            )
        )[0].value as String
    }

    val decimals by lazy {
        (read(
            "decimals",
            inputs = emptyList(),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger).toInt()
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


}