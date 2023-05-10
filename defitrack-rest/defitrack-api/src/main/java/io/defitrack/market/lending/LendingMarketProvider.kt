package io.defitrack.market.lending

import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.BigDecimalExtensions.isZero
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.market.MarketProvider
import io.defitrack.market.farming.domain.InvestmentPreparer
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.token.FungibleToken
import java.math.BigDecimal
import java.math.BigInteger

abstract class LendingMarketProvider : MarketProvider<LendingMarket>() {

    fun create(
        identifier: String,
        name: String,
        token: FungibleToken,
        poolType: String,
        marketSize: BigDecimal? = null,
        rate: BigDecimal? = null,
        positionFetcher: PositionFetcher? = null,
        investmentPreparer: InvestmentPreparer? = null,
        metadata: Map<String, Any> = emptyMap(),
        price: BigDecimal? = null,
        marketToken: FungibleToken?,
        erc20Compatible: Boolean = false
    ): LendingMarket {

        val totalSupply = metadata["totalSupply"] as? BigInteger ?: BigInteger.ZERO

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
            totalSupply = totalSupply.asEth(marketToken?.decimals ?: token.decimals),
            marketToken = marketToken,
            erc20Compatible = erc20Compatible
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
}