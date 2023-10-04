package io.defitrack.protocol.lido

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Company
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.LIDO)
class StethMarketProvider(
    private val lidoService: LidoService,
    private val priceResource: PriceResource
) : FarmingMarketProvider() {
    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val steth = StethContract(
            getBlockchainGateway(),
            lidoService.steth()
        )

        val pooledEth = steth.getPooledEthByShares(steth.getTotalShares())

        return listOf(
            create(
                name = "Liquid Staked Ether 2.0",
                identifier = "steth",
                stakedToken = getToken("0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2").toFungibleToken(),
                rewardTokens = emptyList(),
                farmType = ContractType.STAKING,
                balanceFetcher = PositionFetcher(
                    address = steth.address,
                    function = { user ->
                        steth.sharesOfFunction(user)
                    },
                ),
                marketSize = Refreshable.refreshable {
                    priceResource.calculatePrice(
                        PriceRequest(
                            "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2",
                            getNetwork(),
                            pooledEth.asEth(),
                        )
                    ).toBigDecimal()
                }
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.LIDO
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM

    }
}