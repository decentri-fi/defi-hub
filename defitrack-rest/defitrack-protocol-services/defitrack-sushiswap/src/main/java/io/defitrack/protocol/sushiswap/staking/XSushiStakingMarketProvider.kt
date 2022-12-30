package io.defitrack.protocol.sushiswap.staking

import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.FarmType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.xsushi.XSushiContract
import io.defitrack.token.ERC20Resource
import io.defitrack.token.MarketSizeService
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class XSushiStakingMarketProvider(
    private val erc20Resource: ERC20Resource,
): FarmingMarketProvider() {

    private val xsushi = "0x8798249c2E607446EfB7Ad49eC89dD1865Ff4272"
    private val sushi = "0x6B3595068778DD592e39A122f4f5a5cF09C90fE2"


    override suspend fun fetchMarkets(): List<FarmingMarket> {

        val xSushiContract = XSushiContract(getBlockchainGateway(), "", xsushi)
        val sushiToken = erc20Resource.getTokenInformation(getNetwork(), sushi)
        val totalStakedSushi = erc20Resource.getBalance(getNetwork(), sushi, xsushi)

        val ratio = totalStakedSushi.toBigDecimal().dividePrecisely(xSushiContract.totalSupply().toBigDecimal())

        val marketsize = marketSizeService.getMarketSize(
            sushiToken.toFungibleToken(),
            xsushi,
            getNetwork()
        )

        return listOf(
            create(
                name = "xsushi",
                identifier = "xsushi",
                stakedToken = sushiToken.toFungibleToken(),
                rewardTokens = listOf(sushiToken.toFungibleToken()),
                vaultType = "xsushi",
                marketSize = marketsize,
                apr = null,
                balanceFetcher = PositionFetcher(
                    xsushi,
                    { user ->
                        erc20Resource.balanceOfFunction(xsushi, user, getNetwork())
                    },
                    { retVal ->
                        val userXSushi = (retVal[0].value as BigInteger).toBigDecimal()
                        userXSushi.times(ratio).toBigInteger()
                    }
                ),
                farmType = FarmType.STAKING
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.SUSHISWAP
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}