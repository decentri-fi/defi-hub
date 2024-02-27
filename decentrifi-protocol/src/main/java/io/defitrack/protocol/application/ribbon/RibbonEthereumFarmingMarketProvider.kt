package io.defitrack.protocol.application.ribbon

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.ribbon.RibbonEthereumGraphProvider
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.RIBBON)
class RibbonEthereumFarmingMarketProvider(
    private val ribbonEthereumGraphProvider: RibbonEthereumGraphProvider,
) : FarmingMarketProvider() {

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        return ribbonEthereumGraphProvider.getVaults().map {
            val stakedToken = getToken(it.underlyingAsset)
            create(
                identifier = it.id,
                name = it.name,
                stakedToken = stakedToken,
                rewardToken = stakedToken,
                marketSize = refreshable { //todo: totalBalance is not fetched from the blockchain
                    getPriceResource().calculatePrice(
                        GetPriceCommand(
                            it.underlyingAsset,
                            getNetwork(),
                            it.totalBalance.asEth(stakedToken.decimals),
                            stakedToken.type
                        )
                    ).toBigDecimal()
                },
                positionFetcher = defaultPositionFetcher(it.id),
            )
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.RIBBON
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}