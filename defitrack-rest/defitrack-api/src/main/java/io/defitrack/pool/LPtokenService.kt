package io.defitrack.pool

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.pool.contract.LPTokenContract
import io.github.reactivecircus.cache4k.Cache
import org.springframework.stereotype.Service
import java.util.*

@Service
class LPtokenService(
    private val abiService: ABIResource,
    private val contractAccessorGateway: ContractAccessorGateway
) {

    private val cache = Cache.Builder().build<String, LPTokenContract>()

    val lpABI by lazy {
        abiService.getABI("uniswap/UniswapV2Pair.json")
    }

    suspend fun getLP(network: Network, address: String): LPTokenContract {
        val key = "${network.name}-${address.lowercase(Locale.getDefault())}"
        return cache.get(key) {
            LPTokenContract(
                contractAccessorGateway.getGateway(network),
                lpABI,
                address = address
            )
        }
    }
}