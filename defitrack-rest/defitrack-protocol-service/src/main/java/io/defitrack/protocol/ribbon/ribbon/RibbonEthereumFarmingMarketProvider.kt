package io.defitrack.protocol.ribbon.ribbon

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.price.PriceRequest
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
                        PriceRequest(
                            it.underlyingAsset,
                            getNetwork(),
                            it.totalBalance.asEth(stakedToken.decimals),
                            stakedToken.type
                        )
                    ).toBigDecimal()
                },
                positionFetcher = PositionFetcher(
                    ERC20Contract(getBlockchainGateway(), it.id)::balanceOfFunction
                ),
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