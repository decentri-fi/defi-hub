package io.defitrack.protocol.application.camelot.pooling

import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.common.network.Network
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.uniswap.v2.PairFactoryContract
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.CAMELOT)
class CamelotArbitrumPoolingMarketProvider : PoolingMarketProvider() {


    override suspend fun produceMarkets(): Flow<PoolingMarket> {
        return pairFactoryContract()
            .allPairs()
            .pairsToMarkets()
    }

    private suspend fun pairFactoryContract() = createContract {
        PairFactoryContract("0x6eccab422d763ac031210895c81787e87b43a652")
    }


    override fun getProtocol(): Protocol {
        return Protocol.CAMELOT
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}