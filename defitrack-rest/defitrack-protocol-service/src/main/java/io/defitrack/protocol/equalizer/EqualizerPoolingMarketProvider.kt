package io.defitrack.protocol.equalizer

import arrow.fx.coroutines.parMap
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component
import kotlin.coroutines.EmptyCoroutineContext

@ConditionalOnCompany(Company.EQUALIZER)
@Component
class EqualizerPoolingMarketProvider : PoolingMarketProvider() {

    private val voterAddress = "0x46abb88ae1f2a35ea559925d99fdc5441b592687"

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        val contract = EqualizerVoter(
            getBlockchainGateway(), voterAddress
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.EQUALIZER
    }

    override fun getNetwork(): Network {
        return Network.BASE
    }
}