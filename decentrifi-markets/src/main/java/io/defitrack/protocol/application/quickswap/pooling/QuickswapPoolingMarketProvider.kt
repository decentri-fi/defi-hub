package io.defitrack.protocol.quickswap.pooling

import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.architecture.conditional.ConditionalOnNetwork
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.quickswap.QuickswapService
import io.defitrack.protocol.quickswap.domain.QuickswapPair
import io.defitrack.uniswap.v2.PairFactoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component
import java.time.Duration
import kotlin.coroutines.EmptyCoroutineContext

@Component
@ConditionalOnCompany(Company.QUICKSWAP)
@ConditionalOnNetwork(Network.POLYGON)
class QuickswapPoolingMarketProvider : PoolingMarketProvider() {

    override suspend fun produceMarkets(): Flow<PoolingMarket> {
        val factory = createContract { PairFactoryContract("0x5757371414417b8C6CAad45bAeF941aBc7d3Ab32") }
        return factory.allPairs().pairsToMarkets()
    }

    override fun getProtocol(): Protocol {
        return Protocol.QUICKSWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }

    override fun updateDelta(): Duration {
        return Duration.ofDays(7) //only once a week
    }
}