package io.defitrack.erc20

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.pooling.contract.LPTokenContract
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.util.*

@Service
class LPtokenService(
    private val abiService: ABIResource,
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) {

    private val cache = Cache.Builder().build<String, LPTokenContract>()

    val lpABI by lazy {
        runBlocking {
            abiService.getABI("uniswap/UniswapV2Pair.json")
        }
    }

    suspend fun getLP(network: Network, address: String): LPTokenContract {
        val key = "${network.name}-${address.lowercase(Locale.getDefault())}"
        return cache.get(key) {
            LPTokenContract(
                blockchainGatewayProvider.getGateway(network),
                lpABI,
                address = address
            )
        }
    }
}