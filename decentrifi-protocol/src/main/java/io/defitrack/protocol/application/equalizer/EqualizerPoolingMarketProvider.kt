package io.defitrack.protocol.application.equalizer

import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.common.network.Network
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.equalizer.EqualizerVoter
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Component

@ConditionalOnCompany(Company.EQUALIZER)
@Component
class EqualizerPoolingMarketProvider : PoolingMarketProvider() {

    private val voterAddress = "0x46abb88ae1f2a35ea559925d99fdc5441b592687"

    override suspend fun produceMarkets(): Flow<PoolingMarket> {
        val contract = EqualizerVoter(
            getBlockchainGateway(), voterAddress
        )
        return contract.pools().pairsToMarkets()
    }

    override fun getProtocol(): Protocol {
        return Protocol.EQUALIZER
    }

    override fun getNetwork(): Network {
        return Network.BASE
    }
}