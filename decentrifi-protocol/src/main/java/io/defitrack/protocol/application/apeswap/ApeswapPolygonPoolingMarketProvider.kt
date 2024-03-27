package io.defitrack.protocol.application.apeswap

import arrow.fx.coroutines.parMap
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.LPTokenContract
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.apeswap.ApeswapPolygonService
import io.defitrack.uniswap.v2.PairFactoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.APESWAP)
class ApeswapPolygonPoolingMarketProvider(
    private val apeswapPolygonService: ApeswapPolygonService
) : PoolingMarketProvider() {

    override fun getProtocol(): Protocol {
        return Protocol.APESWAP
    }

    private fun pairFactoryContract() =
        with(getBlockchainGateway()) { PairFactoryContract(apeswapPolygonService.provideFactory()) }

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        pairFactoryContract()
            .allPairs()
            .pairsToMarkets()
            .forEach {
                send(it)
            }
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}