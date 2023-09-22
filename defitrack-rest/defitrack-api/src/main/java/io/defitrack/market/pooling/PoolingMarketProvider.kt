package io.defitrack.market.pooling

import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.BigDecimalExtensions.isZero
import io.defitrack.common.utils.Refreshable
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.MarketProvider
import io.defitrack.market.farming.domain.InvestmentPreparer
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.market.pooling.domain.PoolingMarketTokenShare
import io.defitrack.token.FungibleToken
import io.defitrack.token.TokenType
import java.math.BigDecimal

abstract class PoolingMarketProvider : MarketProvider<PoolingMarket>() {

    suspend fun create(
        name: String,
        identifier: String,
        marketSize: Refreshable<BigDecimal>? = null,
        apr: BigDecimal? = null,
        address: String,
        decimals: Int = 18,
        symbol: String,
        tokenType: TokenType,
        tokens: List<FungibleToken>,
        totalSupply: Refreshable<BigDecimal>,
        positionFetcher: PositionFetcher? = null,
        investmentPreparer: InvestmentPreparer? = null,
        breakdown: List<PoolingMarketTokenShare>? = null,
        erc20Compatible: Boolean = true,
        price: Refreshable<BigDecimal>? = null,
        metadata: Map<String, Any> = emptyMap(),
        internalMetadata: Map<String, Any> = emptyMap(),
        deprecated: Boolean = false,
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
            price = price ?: calculatePrice(marketSize, totalSupply),
            metadata = metadata,
            internalMetadata = internalMetadata,
            deprecated = deprecated
        )
    }

    private suspend fun calculatePrice(
        marketSize: Refreshable<BigDecimal>?,
        totalSupply: Refreshable<BigDecimal>,
    ): Refreshable<BigDecimal> {

        return Refreshable.refreshable {
            if (marketSize == null || marketSize.get() <= BigDecimal.ZERO) return@refreshable BigDecimal.ZERO

            val supply = totalSupply.get()

            if (supply.isZero()) return@refreshable BigDecimal.ZERO

            return@refreshable marketSize.get().dividePrecisely(supply)
        }
    }

    suspend fun fiftyFiftyBreakdown(
        token0: TokenInformationVO,
        token1: TokenInformationVO,
        poolAddress: String
    ): List<PoolingMarketTokenShare> {

        val firstMarketShare = getMarketSize(token0.toFungibleToken(), poolAddress)
        val secondMarketShare = getMarketSize(token1.toFungibleToken(), poolAddress)

        val firstShare = PoolingMarketTokenShare(
            token = token0.toFungibleToken(),
            reserveUSD = if(firstMarketShare == BigDecimal.ZERO) secondMarketShare else firstMarketShare
        )

        val secondShare = PoolingMarketTokenShare(
            token = token1.toFungibleToken(),
            reserveUSD = if(secondMarketShare == BigDecimal.ZERO) firstMarketShare else secondMarketShare
        )
        return listOf(
            firstShare, secondShare
        )
    }
}