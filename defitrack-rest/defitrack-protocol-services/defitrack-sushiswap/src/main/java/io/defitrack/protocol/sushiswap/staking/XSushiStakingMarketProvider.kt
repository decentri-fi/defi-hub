package io.defitrack.protocol.sushiswap.staking

import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.Position
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.xsushi.XSushiContract
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class XSushiStakingMarketProvider(
) : FarmingMarketProvider() {

    private val xsushi = "0x8798249c2E607446EfB7Ad49eC89dD1865Ff4272"
    private val sushi = "0x6B3595068778DD592e39A122f4f5a5cF09C90fE2"

    override suspend fun fetchMarkets(): List<FarmingMarket> {

        val xSushiContract = XSushiContract(getBlockchainGateway(), "", xsushi)
        val sushiToken = getToken(sushi)
        val totalStakedSushi = getERC20Resource().getBalance(getNetwork(), sushi, xsushi)

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
                        getERC20Resource().balanceOfFunction(xsushi, user, getNetwork())
                    },
                    { retVal ->
                        val userXSushi = (retVal[0].value as BigInteger)
                        Position(
                            userXSushi.toBigDecimal().times(ratio).toBigInteger(),
                            userXSushi
                        )
                    }
                ),
                farmType = ContractType.STAKING
            )
        )
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}