package io.defitrack.market.lending

import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.BigDecimalExtensions.isZero
import io.defitrack.common.utils.Refreshable
import io.defitrack.common.utils.refreshable
import io.defitrack.token.FungibleToken
import io.defitrack.market.MarketProvider
import io.defitrack.market.farming.domain.InvestmentPreparer
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.evm.position.PositionFetcher
import java.math.BigDecimal

abstract class LendingMarketProvider : MarketProvider<LendingMarket>() {

    suspend fun create(
        identifier: String,
        name: String,
        token: FungibleToken,
        poolType: String,
        marketSize: Refreshable<BigDecimal>? = null,
        rate: BigDecimal? = null,
        positionFetcher: PositionFetcher? = null,
        investmentPreparer: InvestmentPreparer? = null,
        metadata: Map<String, Any> = emptyMap(),
        price: Refreshable<BigDecimal>? = null,
        marketToken: FungibleToken? = null,
        erc20Compatible: Boolean = false,
        totalSupply: Refreshable<BigDecimal> = refreshable(BigDecimal.ZERO),
        deprecated: Boolean = false,
        internalMetaData: Map<String, Any> = emptyMap(),
    ): LendingMarket {

        return LendingMarket(
            id = "lnd_${getNetwork().slug}-${getProtocol().slug}-${identifier}",
            network = getNetwork(),
            protocol = getProtocol(),
            name = name,
            token = token,
            marketSize = marketSize,
            rate = rate,
            poolType = poolType,
            positionFetcher = positionFetcher,
            investmentPreparer = investmentPreparer,
            metadata = metadata,
            price = price ?: calculatePrice(marketSize, totalSupply),
            totalSupply = totalSupply,
            marketToken = marketToken,
            erc20Compatible = erc20Compatible,
            deprecated = deprecated,
            internalMetaData = internalMetaData
        )
    }

    private suspend fun calculatePrice(
        marketSize: Refreshable<BigDecimal>?,
        totalSupply: Refreshable<BigDecimal>,
    ): Refreshable<BigDecimal> {
        return refreshable {
            if (marketSize == null || marketSize.get() <= BigDecimal.ZERO) return@refreshable BigDecimal.ZERO

            if (totalSupply.get().isZero()) {
                return@refreshable BigDecimal.ZERO
            }

            return@refreshable marketSize.get().dividePrecisely(
                totalSupply.get(),
            )
        }
    }
}