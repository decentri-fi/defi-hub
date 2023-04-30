package io.defitrack.market.pooling

import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.BigDecimalExtensions.isZero
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.MarketProvider
import io.defitrack.market.farming.domain.InvestmentPreparer
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.market.pooling.domain.PoolingMarketTokenShare
import io.defitrack.token.FungibleToken
import io.defitrack.token.TokenType
import java.math.BigDecimal
import java.math.BigInteger

abstract class PoolingMarketProvider : MarketProvider<PoolingMarket>() {


    fun create(
        name: String,
        identifier: String,
        marketSize: BigDecimal? = null,
        apr: BigDecimal? = null,
        address: String,
        decimals: Int = 18,
        symbol: String,
        tokenType: TokenType,
        tokens: List<FungibleToken>,
        totalSupply: BigInteger,
        positionFetcher: PositionFetcher? = null,
        investmentPreparer: InvestmentPreparer? = null,
        breakdown: List<PoolingMarketTokenShare>? = null,
        erc20Compatible: Boolean = true,
        price: BigDecimal? = null
    ): PoolingMarket {
        return PoolingMarket(
            id = "lp_${getNetwork().slug}-${getProtocol().slug}-${identifier}",
            network = getNetwork(),
            protocol = getProtocol(),
            name = name,
            marketSize = marketSize,
            apr = apr,
            address = address,
            decimals = decimals,
            symbol = symbol,
            tokenType = tokenType,
            tokens = tokens,
            positionFetcher = positionFetcher,
            investmentPreparer = investmentPreparer,
            breakdown = breakdown,
            erc20Compatible = erc20Compatible,
            totalSupply = totalSupply,
            price = price ?: calculatePrice(marketSize, totalSupply, decimals)
        )
    }

    private fun calculatePrice(
        marketSize: BigDecimal?,
        totalSupply: BigInteger,
        decimals: Int
    ): BigDecimal {
        if (marketSize == null || marketSize <= BigDecimal.ZERO) return BigDecimal.ZERO
        val supply = totalSupply.asEth(decimals)

        if (supply.isZero()) return BigDecimal.ZERO

        return marketSize.dividePrecisely(
            totalSupply.asEth(decimals),
        )
    }

    suspend fun defaultBreakdown(
        tokens: List<TokenInformationVO>,
        poolAddress: String
    ): List<PoolingMarketTokenShare> {
        return tokens.map {
            PoolingMarketTokenShare(
                token = it.toFungibleToken(),
                reserve = getBalance(it.address, poolAddress),
                reserveUSD = getMarketSize(it.toFungibleToken(), poolAddress)
            )
        }
    }
}