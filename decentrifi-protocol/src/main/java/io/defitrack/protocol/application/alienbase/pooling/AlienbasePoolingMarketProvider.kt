package io.defitrack.protocol.application.alienbase.pooling

import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.architecture.conditional.ConditionalOnNetwork
import io.defitrack.common.network.Network
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.uniswap.v2.PairFactoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.ALIENBASE)
@ConditionalOnNetwork(Network.BASE)
class AlienbasePoolingMarketProvider : PoolingMarketProvider() {

    private val poolFactoryAddress: String = "0x3e84d913803b02a4a7f027165e8ca42c14c0fde7"

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        pairFactoryContract()
            .allPairs()
            .pairsToMarkets()
            .forEach {
                send(it)
            }
    }

    private suspend fun pairFactoryContract() = createContract { PairFactoryContract(poolFactoryAddress) }

    override fun getProtocol(): Protocol {
        return Protocol.ALIENBASE
    }

    override fun getNetwork(): Network {
        return Network.BASE
    }
}