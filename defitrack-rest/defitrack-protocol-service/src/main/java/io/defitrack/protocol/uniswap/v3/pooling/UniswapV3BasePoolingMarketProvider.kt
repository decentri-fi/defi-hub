package io.defitrack.protocol.uniswap.v3.pooling

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.uniswap.v3.prefetch.UniswapV3Prefetcher
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.UNISWAP)
@ConditionalOnProperty(value = ["base.enabled", "uniswapv3.enabled"], havingValue = "true", matchIfMissing = true)
class UniswapV3BasePoolingMarketProvider(
    uniswapV3Prefetcher: UniswapV3Prefetcher
) : UniswapV3PoolingMarketProvider(
    "1371680",
    "0x33128a8fC17869897dcE68Ed026d694621f6FDfD",
    uniswapV3Prefetcher
) {

    override fun getNetwork(): Network {
        return Network.BASE
    }
}