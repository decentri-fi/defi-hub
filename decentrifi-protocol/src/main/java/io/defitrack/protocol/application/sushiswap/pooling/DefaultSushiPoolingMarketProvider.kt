package io.defitrack.protocol.sushiswap.pooling

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMap
import io.defitrack.common.utils.refreshable
import io.defitrack.evm.contract.LPTokenContract
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.sushiswap.contract.SushiV2FactoryContract
import kotlinx.coroutines.flow.channelFlow

abstract class DefaultSushiPoolingMarketProvider(
    private val factoryAddress: String
) : PoolingMarketProvider() {

    override fun getProtocol(): Protocol {
        return Protocol.SUSHISWAP
    }

    override suspend fun produceMarkets() = channelFlow {
        val factory = createContract {
            SushiV2FactoryContract(factoryAddress)
        }

        factory.allPairs()
            .pairsToMarkets()
            .forEach {
                send(it)
            }
    }
}