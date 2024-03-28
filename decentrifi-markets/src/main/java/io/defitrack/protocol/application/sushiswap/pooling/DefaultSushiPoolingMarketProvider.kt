package io.defitrack.protocol.sushiswap.pooling

import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.sushiswap.contract.SushiV2FactoryContract
import kotlinx.coroutines.flow.Flow

abstract class DefaultSushiPoolingMarketProvider(
    private val factoryAddress: String
) : PoolingMarketProvider() {

    override fun getProtocol(): Protocol {
        return Protocol.SUSHISWAP
    }

    override suspend fun produceMarkets(): Flow<PoolingMarket> {
        val factory = createContract {
            SushiV2FactoryContract(factoryAddress)
        }

        return factory.allPairs()
            .pairsToMarkets()
    }
}