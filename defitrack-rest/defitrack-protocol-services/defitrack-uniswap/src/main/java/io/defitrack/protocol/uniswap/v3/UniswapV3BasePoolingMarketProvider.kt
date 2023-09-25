package io.defitrack.protocol.uniswap.v3

import io.defitrack.common.network.Network
import io.defitrack.protocol.uniswap.v3.prefetch.UniswapV3Prefetcher
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = ["base.enabled", "uniswapv3.enabled"], havingValue = "true", matchIfMissing = true)
class UniswapV3BasePoolingMarketProvider(
    uniswapV3Prefetcher: UniswapV3Prefetcher
) : UniswapV3PoolingMarketProvider(
    listOf("1371680"),
    "0x33128a8fC17869897dcE68Ed026d694621f6FDfD",
    uniswapV3Prefetcher
) {

    override fun getNetwork(): Network {
        return Network.BASE
    }
}