package io.defitrack.protocol.balancer.contract

import io.defitrack.abi.TypeUtils.Companion.toBytes32
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.DeprecatedEvmContract
import io.github.reactivecircus.cache4k.Cache
import org.bouncycastle.util.encoders.Hex
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class BalancerVaultContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : DeprecatedEvmContract(
    blockchainGateway, address
) {

    val cache = Cache.Builder<String, List<PoolTokenResult>>().build()

    suspend fun cachePoolTokens(poolIds: List<String>) {
        val results = readMultiCall(
            poolIds.map { poolId ->
                val bytes = Hex.decode(poolId.removePrefix("0x"))
                createFunction(
                    method = "getPoolTokens",
                    inputs = listOf(bytes.toBytes32()),
                    outputs = listOf(
                        object : TypeReference<DynamicArray<Address>>() {},
                        object : TypeReference<DynamicArray<Uint256>>() {},
                        uint256()
                    )
                )
            }
        )

        results.mapIndexed { index, result ->
            poolIds[index] to if (result.success) {
                val tokens = (result.data[0].value as List<Address>).map { it.value as String }
                val balances = (result.data[1].value as List<Uint256>).map { it.value as BigInteger }

                tokens.zip(balances).map {
                    PoolTokenResult(
                        it.first,
                        it.second
                    )
                }
            } else {
                emptyList()
            }
        }.forEach {
            cache.put(it.first, it.second)
        }
    }


    fun getPoolTokens(poolId: String, poolAddress: String): List<PoolTokenResult> {
        val cached = cache.get(poolId)
        return cached?.let {
            it.filter {
                it.token.lowercase() != poolAddress.lowercase()
            }
        } ?: emptyList()
    }

    data class PoolTokenResult(
        val token: String,
        val balance: BigInteger,
    )
}