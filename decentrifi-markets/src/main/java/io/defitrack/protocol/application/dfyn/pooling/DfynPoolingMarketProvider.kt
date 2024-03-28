package io.defitrack.protocol.application.dfyn.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.dfyn.DfynService
import io.defitrack.protocol.application.dfyn.apr.DfynAPRService
import io.defitrack.protocol.dfyn.domain.Pair
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@ConditionalOnCompany(Company.DFYN)
class DfynPoolingMarketProvider(
    private val dfynService: DfynService,
) : PoolingMarketProvider() {

    //TODO: stop using graph
    override suspend fun produceMarkets(): Flow<PoolingMarket> {
        return dfynService.getPairs().map {
            it.id
        }.pairsToMarkets()
    }

    override fun getProtocol(): Protocol {
        return Protocol.DFYN
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}