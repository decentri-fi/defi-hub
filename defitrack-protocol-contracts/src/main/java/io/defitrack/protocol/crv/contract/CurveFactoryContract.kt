package io.defitrack.protocol.crv.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.DeprecatedEvmContract
import kotlinx.coroutines.Deferred
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.StaticArray4
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class CurveFactoryContract(
    blockchainGateway: BlockchainGateway, address: String
): DeprecatedEvmContract(blockchainGateway, address) {

    val poolCount: Deferred<BigInteger> = constant("pool_count", uint256())

    suspend fun getCoins() : Map<String, List<String>> {
        val pools = pools.await()
        return readMultiCall(
            pools.map { pool ->
                createFunction("get_coins", listOf(pool.toAddress()), listOf(
                    object : TypeReference<StaticArray4<Address>>(false) {}
                ))
            }).mapIndexedNotNull { index, it ->
            if(it.success) pools[index] to (it.data[0].value as List<Address>).map { it.value as String } else null
        }.toMap()
    }

    suspend fun getBalances(): Map<String, List<BigInteger>> {
        val pools = pools.await()
        return readMultiCall(
            pools.map { pool ->
                createFunction("get_balances", listOf(pool.toAddress()), listOf(
                    object : TypeReference<StaticArray4<Uint256>>(false) {}
                ))
            }).mapIndexedNotNull { index, it ->
                if(it.success) pools[index] to (it.data[0].value as List<Uint256>).map { it.value as BigInteger } else null
        }.toMap()
    }

    val pools: Deferred<List<String>> = lazyAsync {
        readMultiCall(
            (0 until poolCount.await().toInt()).map {
                createFunction("pool_list", listOf(it.toBigInteger().toUint256()), listOf(TypeUtils.address()))
            }
        ).mapNotNull {
            if(it.success) it.data[0].value as String else null
        }
    }
}