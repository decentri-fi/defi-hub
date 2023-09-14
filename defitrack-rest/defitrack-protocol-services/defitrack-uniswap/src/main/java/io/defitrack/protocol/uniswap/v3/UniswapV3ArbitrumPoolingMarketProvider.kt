package io.defitrack.protocol.uniswap.v3

import io.defitrack.common.network.Network
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("arbitrum.enabled", havingValue = "true", matchIfMissing = true)
class UniswapV3ArbitrumPoolingMarketProvider() : UniswapV3PoolingMarketProvider(
    listOf("165"),
    "0x1f98431c8ad98523631ae4a59f267346ea31f984"
) {

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}