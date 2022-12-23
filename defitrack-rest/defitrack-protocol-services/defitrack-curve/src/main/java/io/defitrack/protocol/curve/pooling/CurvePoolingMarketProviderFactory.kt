package io.defitrack.protocol.curve.pooling

import io.defitrack.common.network.Network
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.protocol.crv.CurvePoolGraphProvider
import io.defitrack.token.ERC20Resource
import io.defitrack.token.MarketSizeService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CurvePoolingMarketProviderFactory {

    @Bean
    fun provideMarketProviders(
        providers: List<CurvePoolGraphProvider>,
        erC20Resource: ERC20Resource,
        marketSizeService: MarketSizeService
    ): List<PoolingMarketProvider> {
        return Network.values().mapNotNull { network ->
            providers.find {
                it.network == network
            }
        }.map {
            CurvePoolingMarketProvider(
                it, marketSizeService, erC20Resource
            )
        }
    }

}