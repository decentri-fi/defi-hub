package io.defitrack.protocol.eigenlayer

import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.common.network.Network
import io.defitrack.evm.position.Position
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.EIGENLAYER)
class EigenlayerStrategyMarketProvider : FarmingMarketProvider() {

    val strategies = listOf(
        "0x93c4b944d05dfe6df7645a86cd2206016c51564d"
    )

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        return strategies.map {
            StrategyContract(
                getBlockchainGateway(),
                it
            )
        }.map { strategy ->
            val underlying = getToken(strategy.underlyingToken.await())
            create(
                name = underlying.name + " Strategy",
                identifier = strategy.address,
                stakedToken = underlying,
                rewardToken = underlying,
                positionFetcher = PositionFetcher(strategy::userUnderlyingView)
            )
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.EIGENLAYER
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}