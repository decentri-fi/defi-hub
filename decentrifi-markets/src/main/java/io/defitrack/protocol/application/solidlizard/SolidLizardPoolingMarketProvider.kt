package io.defitrack.protocol.application.solidlizard

import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.uniswap.v2.PairFactoryContract
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.SOLIDLIZARD)
class SolidLizardPoolingMarketProvider : PoolingMarketProvider() {
    override suspend fun produceMarkets(): Flow<PoolingMarket> {
        val factory = with(getBlockchainGateway()) {
            PairFactoryContract("0x734d84631f00dc0d3fcd18b04b6cf42bfd407074")
        }

        return factory
            .allPairs()
            .pairsToMarkets()
    }

    override fun getProtocol(): Protocol {
        return Protocol.SOLIDLIZARD
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}