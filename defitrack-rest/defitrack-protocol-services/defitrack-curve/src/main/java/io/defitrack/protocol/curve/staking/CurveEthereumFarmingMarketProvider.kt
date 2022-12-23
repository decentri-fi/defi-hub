package io.defitrack.protocol.curve.staking

import io.defitrack.common.network.Network
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.FarmType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.crv.CurveEthereumGaugeGraphProvider
import io.defitrack.token.ERC20Resource
import io.defitrack.token.MarketSizeService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service

@Service
class CurveEthereumFarmingMarketProvider(
    private val curveEthereumGaugeGraphProvider: CurveEthereumGaugeGraphProvider,
    private val erC20Resource: ERC20Resource,
    private val marketSizeService: MarketSizeService,
) : FarmingMarketProvider() {

    override suspend fun fetchMarkets(): List<FarmingMarket> =
        coroutineScope {
            curveEthereumGaugeGraphProvider.getGauges()
                .filter { it.pool != null }
                .map { gauge ->
                    async {
                        try {

                            val stakedToken =
                                erC20Resource.getTokenInformation(getNetwork(), gauge.pool!!.lpToken.address)

                            create(
                                identifier = gauge.address,
                                name = stakedToken.name + " Gauge",
                                stakedToken = stakedToken.toFungibleToken(),
                                rewardTokens = emptyList(),
                                vaultType = "curve-gauge",
                                marketSize = marketSizeService.getMarketSize(
                                    stakedToken.toFungibleToken(), gauge.address, getNetwork()
                                ),
                                farmType = FarmType.LIQUIDITY_MINING,
                                balanceFetcher = PositionFetcher(
                                    gauge.address,
                                    { user ->
                                        erC20Resource.balanceOfFunction(gauge.address, user, getNetwork())
                                    }
                                )
                            )
                        } catch (ex: Exception) {
                            logger.error("Unable to fetch curve gauge ${gauge.address}")
                            null
                        }
                    }
                }.awaitAll().filterNotNull()
        }

    override fun getProtocol(): Protocol {
        return Protocol.CURVE
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}