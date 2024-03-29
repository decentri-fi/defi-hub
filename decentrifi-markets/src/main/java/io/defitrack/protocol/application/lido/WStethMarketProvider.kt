package io.defitrack.protocol.application.lido

import io.defitrack.adapter.output.domain.market.GetPriceCommand
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.refreshable
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.lido.LidoService
import io.defitrack.protocol.lido.StethContract
import io.defitrack.protocol.lido.WSTEthContract
import org.springframework.stereotype.Component
import java.math.BigDecimal

private const val STETH = "0xae7ab96520DE3A18E5e111B5EaAb095312D7fE84"
private const val WETH = "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2"

@Component
@ConditionalOnCompany(Company.LIDO)
class WStethMarketProvider(
    private val lidoService: LidoService
) : FarmingMarketProvider() {

    override suspend fun fetchMarkets(): List<FarmingMarket> {

        val eth = getToken(WETH)

        return listOf(
            create(
                name = "Wrapped Liquid Staked Ether 2.0",
                identifier = "wsteth",
                stakedToken = eth,
                rewardToken = eth,
                type = "lido.wrapped-staked-eth",
                marketSize = refreshable {
                    calculateMarketSize()
                }
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.LIDO
    }

    suspend fun calculateMarketSize(): BigDecimal = with(getBlockchainGateway()) {
        val wstEthContract = WSTEthContract(
            lidoService.wsteth()
        )
        val totalTokens = wstEthContract.totalSupply().get()
        val wrappedStethTokens = wstEthContract.getStethByWstethFunction(totalTokens)

        val stEthContract = StethContract(
            getBlockchainGateway(),
            STETH
        )
        val pooledEth = stEthContract.getPooledEthByShares(wrappedStethTokens)
        return getPriceResource().calculatePrice(
            GetPriceCommand(
                WETH,
                getNetwork(),
                pooledEth.asEth()
            )
        ).toBigDecimal()
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}