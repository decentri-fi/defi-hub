package io.defitrack.protocol.curve.pooling

import io.defitrack.market.pooling.PoolingPositionProvider
import io.defitrack.market.pooling.StandardLpPositionProvider
import io.defitrack.token.ERC20Resource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CurvePoolingPositionProviderFactory(
    private val curvePoolingMarketProviders: List<CurvePoolingMarketProvider>,
) {
    @Bean
    fun providePositionProviders(erC20Resource: ERC20Resource): List<PoolingPositionProvider> {
        return curvePoolingMarketProviders.map {
            StandardLpPositionProvider(it, erC20Resource)
        }
    }
}