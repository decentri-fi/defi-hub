package io.defitrack.protocol.pancakeswap

import arrow.core.Either
import arrow.core.Option
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.domain.PoolingMarketTokenShare
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.uniswap.v3.UniswapV3PoolContract
import io.defitrack.uniswap.v3.UniswapV3PoolFactoryContract
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component
import kotlin.coroutines.EmptyCoroutineContext

@Component
@ConditionalOnCompany(Company.PANCAKESWAP)
class PancakeswapV3PoolingMarketProvider(
    private val pancakeswapV3Prefetcher: PancakeswapV3Prefetcher
) : PoolingMarketProvider() {

    val startBlock = "750149"

    val prefetches = lazyAsync {
        pancakeswapV3Prefetcher.getPrefetches(getNetwork())
    }

    val poolFactoryAddress = "0x0bfbcf9fa4f9c56b0f40a671ad40e0805a091865"

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        uniswapV3PoolFactoryContract.await().getPools(startBlock).parMapNotNull(EmptyCoroutineContext, 12) {
            marketFromCache(it)
        }.forEach {
            it.getOrNull()?.let {
                send(it)
            }
        }
    }

    val uniswapV3PoolFactoryContract = lazyAsync {
        UniswapV3PoolFactoryContract(
            getBlockchainGateway(),
            poolFactoryAddress
        )
    }

    val poolCache = Cache.Builder<String, Option<PoolingMarket>>().build()

    suspend fun marketFromCache(poolAddress: String) = poolCache.get(poolAddress.lowercase()) {
        getMarket(poolAddress).mapLeft { throwable ->
            logger.error("error getting market for ${throwable.message}")
        }.getOrNone()
    }

    suspend fun getMarket(address: String): Either<Throwable, PoolingMarket> {
        return Either.catch {
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


            val totalSupply = prefetch?.totalSupply ?: pool.liquidity.await().asEth()
            create(
                identifier = identifier,
                name = "${token0.symbol}/${token1.symbol}",
                address = pool.address,
                symbol = "${token0.symbol}-${token1.symbol}",
                breakdown = breakdown,
                totalSupply = refreshable(totalSupply) {
                    pool.refreshLiquidity().asEth()
                },
                erc20Compatible = false,
                internalMetadata = mapOf("contract" to pool),
            )
        }
    }


    override fun getProtocol(): Protocol {
        return Protocol.PANCAKESWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON_ZKEVM
    }
}