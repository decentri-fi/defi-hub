package io.defitrack.protocol.uniswap.v3.pooling

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.core.Option
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.map
import io.defitrack.common.utils.refreshable
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.market.pooling.domain.PoolingMarketTokenShare
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.uniswap.v3.prefetch.UniswapV3Prefetcher
import io.defitrack.uniswap.v3.UniswapV3PoolContract
import io.defitrack.uniswap.v3.UniswapV3PoolFactoryContract
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.math.BigDecimal
import kotlin.coroutines.EmptyCoroutineContext

abstract class UniswapV3PoolingMarketProvider(
    private val startBlock: String,
    private val poolFactoryAddress: String,
    private val uniswapV3Prefetcher: UniswapV3Prefetcher
) : PoolingMarketProvider() {

    val poolFactory = lazyAsync {
        UniswapV3PoolFactoryContract(
            getBlockchainGateway(), poolFactoryAddress
        )
    }

    val prefetches = lazyAsync {
        uniswapV3Prefetcher.getPrefetches(getNetwork())
    }

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        poolFactory.await().getPools(startBlock).parMapNotNull(EmptyCoroutineContext, 12) {
            marketFromCache(it)
        }.forEach {
            it.getOrNull()?.let {
                send(it)
            }
        }
    }

    val poolCache = Cache.Builder<String, Option<PoolingMarket>>().build()

    suspend fun marketFromCache(poolAddress: String) = poolCache.get(poolAddress.lowercase()) {
        getMarket(poolAddress).mapLeft { throwable ->
            when (throwable) {
                is MarketTooLowException -> logger.debug("market too low for ${throwable.message}")
                else -> logger.error("error getting market for ${throwable.message}")
            }
        }.getOrNone()
    }

    suspend fun getMarket(address: String): Either<Throwable, PoolingMarket> {
        return catch {
            val identifier = "v3-${address}"

            val prefetch = prefetches.await().find {
                it.id == createId(identifier)
            }

            val pool = UniswapV3PoolContract(getBlockchainGateway(), address)
            val token0 = prefetch?.tokens?.get(0) ?: getToken(pool.token0.await())
            val token1 = prefetch?.tokens?.get(1) ?: getToken(pool.token1.await())

            val breakdown = refreshable(
                prefetch?.breakdown?.map {
                    PoolingMarketTokenShare(
                        it.token,
                        it.reserve,
                        it.reserveUSD
                    )
                } ?: fiftyFiftyBreakdown(token0, token1, address)
            ) {
                fiftyFiftyBreakdown(token0, token1, address)
            }

            val marketSize = breakdown.map {
                it.sumOf(PoolingMarketTokenShare::reserveUSD)
            }

            if (marketSize.get() != BigDecimal.ZERO && marketSize.get() > BigDecimal.valueOf(10000)) {
                val totalSupply = prefetch?.totalSupply ?: pool.liquidity.await().asEth()
                create(
                    identifier = identifier,
                    name = "${token0.symbol}/${token1.symbol}",
                    address = pool.address,
                    symbol = "${token0.symbol}-${token1.symbol}",
                    breakdown = breakdown,
                    tokens = listOf(token0, token1),
                    marketSize = marketSize,
                    positionFetcher = null,
                    totalSupply = refreshable(totalSupply) {
                        pool.refreshLiquidity().asEth()
                    },
                    erc20Compatible = false,
                    internalMetadata = mapOf("contract" to pool),
                )
            } else {
                throw MarketTooLowException("market size is zero for ${pool.address}")
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.UNISWAP_V3
    }

    class MarketTooLowException(msg: String) : RuntimeException(msg)
}