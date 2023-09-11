package io.defitrack.protocol.uniswap.v3

import io.defitrack.common.network.Network
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("ethereum.enabled", havingValue = "true", matchIfMissing = true)
class UniswapV3EthereumPoolingMarketProvider() : UniswapV3PoolingMarketProvider(
    listOf("12629885", "15090817"),
    "0x1f98431c8ad98523631ae4a59f267346ea31f984"
) {

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}