package io.defitrack.market.pooling

import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.BigDecimalExtensions.isZero
import io.defitrack.common.utils.RefetchableValue
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
        marketSize: RefetchableValue<BigDecimal>? = null,
        apr: BigDecimal? = null,
        address: String,
        decimals: Int = 18,
        symbol: String,
        tokenType: TokenType,
        tokens: List<FungibleToken>,
        totalSupply: RefetchableValue<BigDecimal>,
        positionFetcher: PositionFetcher? = null,
        investmentPreparer: InvestmentPreparer? = null,
        breakdown: List<PoolingMarketTokenShare>? = null,
        erc20Compatible: Boolean = true,
        price: RefetchableValue<BigDecimal>? = null,
        metadata: Map<String, Any> = emptyMap()
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
            metadata = metadata
        )
    }

    private suspend fun calculatePrice(
        marketSize: RefetchableValue<BigDecimal>?,
        totalSupply: RefetchableValue<BigDecimal>,
    ): RefetchableValue<BigDecimal> {

        return RefetchableValue.refetchable {
            if (marketSize == null || marketSize.get() <= BigDecimal.ZERO) return@refetchable BigDecimal.ZERO

            val supply = totalSupply.get()

            if (supply.isZero()) return@refetchable BigDecimal.ZERO

            return@refetchable marketSize.get().dividePrecisely(supply)
        }
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