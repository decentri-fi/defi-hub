package io.defitrack.protocol.application.lido

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.price.port.`in`.PricePort
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.lido.LidoService
import io.defitrack.protocol.lido.StethContract
import org.springframework.stereotype.Component

private const val WETH = "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2"

@Component
@ConditionalOnCompany(Company.LIDO)
class LidoStakedEtherProvider(
    private val lidoService: LidoService,
    private val priceResource: PricePort,
) : FarmingMarketProvider() {

    override suspend fun fetchMarkets(): List<FarmingMarket> {

        val eth = getToken(WETH)

        val steth = StethContract(
            getBlockchainGateway(),
            lidoService.steth()
        )

        val pooledEth = steth.getPooledEthByShares(steth.getTotalShares())

        return listOf(
            create(
                name = "Liquid Staked Ether 2.0",
                identifier = "steth",
                stakedToken = eth,
                rewardToken = eth,
                positionFetcher = PositionFetcher(
                    functionCreator = steth::sharesOfFunction,
                ),
                marketSize = refreshable {
                    priceResource.calculatePrice(
                        GetPriceCommand(
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