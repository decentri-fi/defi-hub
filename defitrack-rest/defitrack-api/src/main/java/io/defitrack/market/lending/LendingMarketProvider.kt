package io.defitrack.market.lending

import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.BigDecimalExtensions.isZero
import io.defitrack.common.utils.RefetchableValue
import io.defitrack.market.MarketProvider
import io.defitrack.market.farming.domain.InvestmentPreparer
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.token.FungibleToken
import java.math.BigDecimal

abstract class LendingMarketProvider : MarketProvider<LendingMarket>() {

    suspend fun create(
        identifier: String,
        name: String,
        token: FungibleToken,
        poolType: String,
        marketSize: RefetchableValue<BigDecimal>? = null,
        rate: BigDecimal? = null,
        positionFetcher: PositionFetcher? = null,
        investmentPreparer: InvestmentPreparer? = null,
        metadata: Map<String, Any> = emptyMap(),
        price: RefetchableValue<BigDecimal>? = null,
        marketToken: FungibleToken?,
        erc20Compatible: Boolean = false,
        totalSupply: RefetchableValue<BigDecimal>
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
            price = price ?: calculatePrice(marketSize, totalSupply, marketToken?.decimals ?: token.decimals),
            totalSupply = totalSupply,
            marketToken = marketToken,
            erc20Compatible = erc20Compatible
        )
    }

    private suspend fun calculatePrice(
        marketSize: RefetchableValue<BigDecimal>?,
        totalSupply: RefetchableValue<BigDecimal>,
        decimals: Int
    ): RefetchableValue<BigDecimal> {
        return RefetchableValue.refetchable {
            if (marketSize == null || marketSize.get() <= BigDecimal.ZERO) return@refetchable BigDecimal.ZERO

            val supply = totalSupply.get()

            if (supply.isZero()) {
                return@refetchable BigDecimal.ZERO
            }

            return@refetchable marketSize.get().dividePrecisely(supply)
        }
    }
}