package io.defitrack.protocol.application.uniswap.v3.pooling

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.core.None
import arrow.core.Option
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.LazyValue
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.map
import io.defitrack.common.utils.refreshable
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.domain.PoolingMarketTokenShare
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.uniswap.v3.prefetch.UniswapV3Prefetcher
import io.defitrack.uniswap.v3.UniswapV3PoolContract
import io.defitrack.uniswap.v3.UniswapV3PoolFactoryContract
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.math.BigDecimal

abstract class UniswapV3PoolingMarketProvider(
    private val startBlock: String,
    private val poolFactoryAddress: String,
    private val uniswapV3Prefetcher: UniswapV3Prefetcher
) : PoolingMarketProvider() {

    val prefetches = LazyValue {
        uniswapV3Prefetcher.getPrefetches(getNetwork())
    }

    val poolFactory by lazy {
        createPoolFactory()
    }

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        val pools = createPoolFactory().getPools(startBlock)
            .map {
                with(getBlockchainGateway()) { UniswapV3PoolContract(it) }
            }
            .resolve()

        pools.parMapNotNull(concurrency = 12) {
            val pair = it.address to getMarket(it).mapLeft {
                when (it) {
                    is MarketTooLowException -> logger.trace("market too low for ${it.message}")
                    is UnverifiedTokensException -> logger.debug("unverified tokens for ${it.message}")
                    else -> logger.error("error getting market for ${it.message}")
                }
            }.getOrNone()
            poolCache.put(pair.first.lowercase(), pair.second)

            pair.second.onSome {
                send(it)
            }
        }
    }

    private fun createPoolFactory() = UniswapV3PoolFactoryContract(
        getBlockchainGateway(), poolFactoryAddress
    )

    val poolCache = Cache.Builder<String, Option<PoolingMarket>>().build()
    fun marketFromCache(poolAddress: String) = poolCache.get(poolAddress.lowercase()) ?: None

    suspend fun getMarket(market: UniswapV3PoolContract): Either<Throwable, PoolingMarket> {
        return catch {
            val identifier = "v3-${market.address}"

            val prefetch = prefetches.get().find {
                it.id == createId(identifier)
            }


            val token0 = prefetch?.tokens?.get(0)?.toFungibleToken(getNetwork()) ?: getToken(market.token0.await())
            val token1 = prefetch?.tokens?.get(1)?.toFungibleToken(getNetwork()) ?: getToken(market.token1.await())



            listOf(token0, token1).map {
                it.address.lowercase()
            }.any {
                !allVerifiedTokens.await().contains(it)
            }.let {
                if (it) {
                    throw UnverifiedTokensException("Unverified tokens in market $identifier")
                }
            }

            val breakdown = refreshable(
                prefetch?.breakdown?.map {
                    PoolingMarketTokenShare(
                        it.token.toFungibleToken(getNetwork()),
                        it.reserve
                    )
                } ?: breakdownOf(market.address, token0, token1)
            ) {
                breakdownOf(market.address, token0, token1)
            }


            val totalSupply = prefetch?.totalSupply ?: market.liquidity.await().asEth()
            create(
                identifier = identifier,
                name = "${token0.symbol}/${token1.symbol}",
                address = market.address,
                symbol = "${token0.symbol}-${token1.symbol}",
                breakdown = breakdown,
                positionFetcher = null,
                totalSupply = refreshable(totalSupply) {
                    market.refreshLiquidity().asEth()
                },
                erc20Compatible = false,
                internalMetadata = mapOf("contract" to market),
            )
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.UNISWAP_V3
    }

    val allVerifiedTokens = lazyAsync {
        getERC20Resource().getAllTokens(getNetwork(), true).map {
            it.address.lowercase()
        }
    }

    class MarketTooLowException(msg: String) : RuntimeException(msg)
    class UnverifiedTokensException(msg: String) : RuntimeException(msg)
}