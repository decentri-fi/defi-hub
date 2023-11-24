package io.defitrack.protocol.bancor.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.dynamicArray
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicArray
import java.math.BigInteger

class BancorNetworkContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : EvmContract(
    blockchainGateway, address
) {

    suspend fun liquidityPools(): List<String> {
        val underlyings = (read(
            "liquidityPools",
            outputs = listOf(
                object : TypeReference<DynamicArray<Address>>() {}
            )
        ).first().value as List<Address>).map {
            it.value as String
        }

        val poolCollections = readMultiCall(
            underlyings.map {
                collectionByPool(it)
            }
        )

        return poolCollections.filter {
            it.success
        }.map { it.data.first().value as String }.distinctBy {
            it.lowercase()
        }.map {
            BancorPoolCollectionContract(
                blockchainGateway,
                it
            ).allPools()
        }.flatten()
    }

    suspend fun collectionByPool(pool: String): ContractCall {
        return createFunction(
            "collectionByPool",
            inputs = listOf(pool.toAddress()),
            outputs = listOf(TypeUtils.address())
        )
    }

    fun depositFunction(token: String, amount: BigInteger): ContractCall {
        return createFunction(
            "deposit",
            listOf(
                token.toAddress(),
                amount.toUint256()
            )
        )
    }
}