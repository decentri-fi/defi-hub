package io.defitrack.pool

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.EvmContractAccessor
import io.defitrack.pool.contract.LPTokenContract
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.util.*

@Service
class LPtokenService(
    private val abiService: ABIResource,
    private val contractAccessors: List<EvmContractAccessor>
) {

    private val cache = Cache.Builder().build<String, LPTokenContract>()

    val lpABI by lazy {
        abiService.getABI("uniswap/UniswapV2Pair.json")
    }

    fun getLP(network: Network, address: String): LPTokenContract {
        val key = "${network.name}-${address.lowercase(Locale.getDefault())}"
        return runBlocking(Dispatchers.IO) {
            cache.get(key) {
                LPTokenContract(
                    getContractAccessor(network),
                    lpABI,
                    address = address
                )
            }
        }
    }

    fun getContractAccessor(network: Network): EvmContractAccessor {
        return contractAccessors.find {
            it.getNetwork() == network
        } ?: throw IllegalArgumentException("$network not supported")
    }
}