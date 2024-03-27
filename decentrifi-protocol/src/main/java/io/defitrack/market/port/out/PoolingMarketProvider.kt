package io.defitrack.market.port.out

import arrow.core.Either
import arrow.fx.coroutines.parMap
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
import io.defitrack.evm.contract.LPTokenContract
import io.defitrack.market.domain.asShare
import java.math.BigDecimal

abstract class PoolingMarketProvider : MarketProvider<PoolingMarket>() {

    fun create(
        name: String,
        identifier: String,
        apr: BigDecimal? = null,
        address: String,
        decimals: Int = 18,
        symbol: String,
        type: String = "pool",
        totalSupply: Refreshable<BigDecimal>,
        positionFetcher: PositionFetcher? = null,
        investmentPreparer: InvestmentPreparer? = null,
        breakdown: Refreshable<List<PoolingMarketTokenShare>>,
        erc20Compatible: Boolean = true,
        metadata: Map<String, Any> = emptyMap(),
        internalMetadata: Map<String, Any> = emptyMap(),
        deprecated: Boolean = false
    ): PoolingMarket {
        return PoolingMarket(
            id = createId(identifier),
            network = getNetwork(),
            protocol = getProtocol(),
            name = name,
            apr = apr,
            address = address,
            decimals = decimals,
            symbol = symbol,
            positionFetcher = positionFetcher,
            investmentPreparer = investmentPreparer,
            breakdown = breakdown,
            erc20Compatible = erc20Compatible,
            totalSupply = totalSupply,
            metadata = metadata,
            internalMetadata = internalMetadata,
            deprecated = deprecated,
            type = type,
        )
    }


    suspend fun List<String>.pairsToMarkets(): List<PoolingMarket> {
        return this.map {
            createContract {
                LPTokenContract(it)
            }
        }.resolve()
            .parMap(concurrency = 12) { lpContract ->
                create(lpContract)
            }.mapNotNull {
                it.mapLeft {
                    logger.error("Failed to create market: {}", it.message)
                }.getOrNull()
            }
    }

    protected suspend fun create(lpToken: LPTokenContract): Either<Throwable, PoolingMarket> {
        return Either.catch {

            val token0 = getToken(lpToken.token0.await())
            val token1 = getToken(lpToken.token1.await())

            val breakdown = refreshable {
                breakdownOf(
                    lpToken.address,
                    token0, token1
                )
            }

            create(
                address = lpToken.address,
                name = breakdown.get().joinToString("/") { it.token.name },
                symbol = breakdown.get().joinToString("/") { it.token.symbol },
                breakdown = breakdown,
                identifier = lpToken.address,
                positionFetcher = defaultPositionFetcher(lpToken.address),
                totalSupply = lpToken.totalDecimalSupply()
            )
        }
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

    //TODO: make refreshable
    suspend fun breakdownOf(
        poolAddress: String,
        vararg args: FungibleTokenInformation
    ): List<PoolingMarketTokenShare> {
        return args.map { token ->
            token.asShare(getBalance(token.address, poolAddress))
        }
    }

    @Deprecated("use breakdownOf")
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