package io.defitrack.protocol.lido

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.Company
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component
import java.math.BigDecimal

private const val STETH = "0xae7ab96520DE3A18E5e111B5EaAb095312D7fE84"
private const val WSTETH = "0x7f39c581f595b53c5cb19bd0b3f8da6c935e2ca0"
private const val WETH = "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2"

@Component
@ConditionalOnCompany(Company.LIDO)
class WStethMarketProvider(
) : FarmingMarketProvider() {

    val wstEthContract by lazy {
        WSTEthContract(
            getBlockchainGateway(),
            WSTETH
        )
    }

    val stEthContract by lazy {
        StethContract(
            getBlockchainGateway(),
            STETH
        )
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> {

        val eth = getToken(WETH).toFungibleToken()


        return listOf(
            create(
                name = "Wrapped Liquid Staked Ether 2.0",
                identifier = "wsteth",
                stakedToken = eth,
                rewardTokens = emptyList(),
                farmType = ContractType.STAKING,
                marketSize = Refreshable.refreshable {
                    calculateMarketSize()
                }
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.LIDO
    }

    suspend fun calculateMarketSize(): BigDecimal {
        val totalTokens = wstEthContract.totalSupply()
        val wrappedStethTokens = wstEthContract.getStethByWstethFunction(totalTokens)
        val pooledEth = stEthContract.getPooledEthByShares(wrappedStethTokens)
        return getPriceResource().calculatePrice(
            PriceRequest(
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