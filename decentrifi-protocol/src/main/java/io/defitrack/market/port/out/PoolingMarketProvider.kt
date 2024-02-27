package io.defitrack.market.port.out

import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.BigDecimalExtensions.isZero
import io.defitrack.common.utils.Refreshable
import io.defitrack.common.utils.map
import io.defitrack.common.utils.refreshable
import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.invest.InvestmentPreparer
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.domain.PoolingMarketTokenShare
import io.defitrack.event.HistoricEventExtractor
import java.math.BigDecimal

abstract class PoolingMarketProvider : MarketProvider<PoolingMarket>() {

    suspend fun create(
        name: String,
        identifier: String,
        apr: BigDecimal? = null,
        address: String,
        decimals: Int = 18,
        symbol: String,
        type: String = "pool",
        tokens: List<FungibleTokenInformation>? = null,
        totalSupply: Refreshable<BigDecimal>,
        positionFetcher: PositionFetcher? = null,
        investmentPreparer: InvestmentPreparer? = null,
        breakdown: Refreshable<List<PoolingMarketTokenShare>>? = null,
        erc20Compatible: Boolean = true,
        metadata: Map<String, Any> = emptyMap(),
        internalMetadata: Map<String, Any> = emptyMap(),
        deprecated: Boolean = false,
        historicEventExtractor: HistoricEventExtractor? = null,
    ): PoolingMarket {
        val actualTokens = tokens ?: breakdown?.get()?.map {
            it.token
        } ?: emptyList()

        return PoolingMarket(
            id = createId(identifier),
            network = getNetwork(),
            protocol = getProtocol(),
            name = name,
            apr = apr,
            address = address,
            decimals = decimals,
            symbol = symbol,
            tokens = actualTokens,
            positionFetcher = positionFetcher,
            investmentPreparer = investmentPreparer,
            breakdown = breakdown,
            erc20Compatible = erc20Compatible,
            totalSupply = totalSupply,
            metadata = metadata,
            internalMetadata = internalMetadata,
            deprecated = deprecated,
            type = type,
            historicEventExtractor = historicEventExtractor,
        )
    }

    fun createId(identifier: String) = "lp_${getNetwork().slug}-${getProtocol().slug}-${identifier}"

    suspend fun calculatePrice(
        marketSize: Refreshable<BigDecimal>?,
        totalSupply: Refreshable<BigDecimal>,
    ): Refreshable<BigDecimal> {
        return refreshable {
            if (marketSize == null || marketSize.get() <= BigDecimal.ZERO) return@refreshable BigDecimal.ZERO

            val supply = totalSupply.get()

            if (supply.isZero()) return@refreshable BigDecimal.ZERO

            return@refreshable marketSize.get().dividePrecisely(supply)
        }
    }

    suspend fun fiftyFiftyBreakdown(
        token0: FungibleTokenInformation,
        token1: FungibleTokenInformation,
        poolAddress: String
    ): List<PoolingMarketTokenShare> {

        val firstShare = PoolingMarketTokenShare(
            token = token0,
            reserve = getBalance(token0.address, poolAddress),
        )

        val secondShare = PoolingMarketTokenShare(
            token = token1,
            reserve = getBalance(token1.address, poolAddress),
        )
        return listOf(
            firstShare, secondShare
        )
    }
}