package io.defitrack.protocol.uniswap.v3.pooling

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.uniswap.v3.prefetch.UniswapV3Prefetcher
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.UNISWAP)
@ConditionalOnProperty(value = ["arbitrum.enabled", "uniswapv3.enabled"], havingValue = "true", matchIfMissing = true)
class UniswapV3ArbitrumPoolingMarketProvider(
    uniswapV3Prefetcher: UniswapV3Prefetcher
) : UniswapV3PoolingMarketProvider(
    listOf("165"),
    "0x1f98431c8ad98523631ae4a59f267346ea31f984",
    uniswapV3Prefetcher
) {

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}