package io.defitrack.staking

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.adamant.StethContract
import io.defitrack.protocol.adamant.WSTEthContract
import org.springframework.stereotype.Component

private const val STETH = "0xae7ab96520DE3A18E5e111B5EaAb095312D7fE84"
private const val WSTETH = "0x7f39c581f595b53c5cb19bd0b3f8da6c935e2ca0"
private const val WETH = "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2"

@Component
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
                vaultType = "wsteth",
                farmType = ContractType.STAKING,
                marketSize = calculateMarketSize().toBigDecimal()
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.LIDO
    }

    suspend fun calculateMarketSize(): Double {
        val totalTokens = wstEthContract.getTotalSupply()
        val wrappedStethTokens = wstEthContract.getStethByWstethFunction(totalTokens)
        val pooledEth = stEthContract.getPooledEthByShares(wrappedStethTokens)
        return getPriceResource().calculatePrice(
            PriceRequest(
                WETH,
                getNetwork(),
                pooledEth.asEth()
            )
        )
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}