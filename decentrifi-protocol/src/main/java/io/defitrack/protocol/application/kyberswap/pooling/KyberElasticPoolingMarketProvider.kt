package io.defitrack.protocol.application.kyberswap.pooling

import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.common.network.Network
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.kyberswap.contract.KyberswapElasticContract
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.KYBER_SWAP)
class KyberElasticPoolingMarketProvider : PoolingMarketProvider() {

    override suspend fun produceMarkets(): Flow<PoolingMarket> {
        val contract = createContract {
            KyberswapElasticContract(
                "0xb85ebe2e4ea27526f817ff33fb55fb240057c03f"
            )
        }

        return contract.allPairs().map { it.address }
            .pairsToMarkets()
    }

    override fun getProtocol(): Protocol {
        return Protocol.KYBER_SWAP
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}