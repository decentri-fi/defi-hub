package io.defitrack.protocol.application.eigenlayer

import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.common.network.Network
import io.defitrack.evm.position.Position
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.eigenlayer.StrategyContract
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.EIGENLAYER)
class EigenlayerStrategyMarketProvider : FarmingMarketProvider() {

    val strategies = listOf(
        "0x93c4b944d05dfe6df7645a86cd2206016c51564d",
        "0x0fe4f44bee93503346a3ac9ee5a26b130a5796d6",
        "0x1bee69b7dfffa4e2d53c2a2df135c388ad25dcd2",
        "0x9d7ed45ee2e8fc5482fa2428f15c971e6369011d",
        "0x54945180db7943c0ed0fee7edab2bd24620256bc",
        "0x57ba429517c3473b6d34ca9acd56c0e735b94c02",
        "0x7ca911e83dabf90c90dd3de5411a10f1a6112184",
        "0xa4c637e0f704745d182e4d38cab7e7485321d059",
        "0x13760f50a9d7377e4f20cb8cf9e4c26586c658ff"
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
                positionFetcher = PositionFetcher(strategy::userUnderlyingView),
                type = "eigenlayer.restaking-strategy"
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