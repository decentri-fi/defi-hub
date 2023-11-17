package io.defitrack.protocol.pancakeswap

import arrow.core.Either
import arrow.core.Option
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.abi.TypeUtils
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.event.EventDecoder.Companion.extract
import io.defitrack.evm.GetEventLogsCommand
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.market.pooling.domain.PoolingMarketTokenShare
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.uniswap.v3.pooling.UniswapV3PoolingMarketProvider
import io.defitrack.uniswap.v3.UniswapV3PoolContract
import io.defitrack.uniswap.v3.UniswapV3PoolFactoryContract
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component
import org.web3j.abi.datatypes.Event
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.coroutines.EmptyCoroutineContext

@Component
@ConditionalOnCompany(Company.PANCAKESWAP)
class PancakeswapV3PoolingMarketProvider(
    private val pancakeswapV3Prefetcher: PancakeswapV3Prefetcher
) : PoolingMarketProvider() {

    val fromBlocks = listOf(
        "750149"
    )

    val poolCreatedEvent = Event(
        "PoolCreated", listOf(
            TypeUtils.address(true),
            TypeUtils.address(true),
            TypeUtils.uint24(true),
            TypeUtils.int24(),
            TypeUtils.address(false)
        )
    )

    val prefetches = lazyAsync {
        pancakeswapV3Prefetcher.getPrefetches(getNetwork())
    }


    val poolFactoryAddress = "0x0bfbcf9fa4f9c56b0f40a671ad40e0805a091865"
    val poolFactory = lazyAsync {
        UniswapV3PoolFactoryContract(
            getBlockchainGateway(), poolFactoryAddress
        )
    }

    val poolAddresses = lazyAsync {
        fromBlocks.mapIndexed { index, block ->
            async {
                getLogsBetweenBlocks(block, fromBlocks.getOrNull(index + 1))
            }
        }.awaitAll().flatten()
    }

    suspend fun getLogsBetweenBlocks(fromBlock: String, toBlock: String?): List<String> {
        val gateway = getBlockchainGateway()
        val logs = gateway.getEventsAsEthLog(
            GetEventLogsCommand(
                addresses = listOf(poolFactoryAddress),
                topic = "0x783cca1c0412dd0d695e784568c96da2e9c22ff989357a2e8b1d9b2b4e6b7118",
                fromBlock = BigInteger(fromBlock, 10),
                toBlock = toBlock?.let { BigInteger(toBlock, 10) }
            )
        )

        return logs.map {
            poolCreatedEvent.extract(it, false, 1) as String
        }
    }

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        poolAddresses.await().parMapNotNull(EmptyCoroutineContext, 12) {
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
            val token0 = prefetch?.tokens?.get(0) ?: getToken(pool.token0.await()).toFungibleToken()
            val token1 = prefetch?.tokens?.get(1) ?: getToken(pool.token1.await()).toFungibleToken()

            val breakdown = prefetch?.breakdown?.map {
                PoolingMarketTokenShare(
                    it.token,
                    it.reserve,
                    it.reserveUSD
                )
            } ?: fiftyFiftyBreakdown(token0, token1, address)

            val marketSize = breakdown.sumOf {
                it.reserveUSD
            }

            val totalSupply = prefetch?.totalSupply ?: pool.liquidity.await().asEth()
            create(
                identifier = identifier,
                name = "${token0.symbol}/${token1.symbol}",
                address = pool.address,
                symbol = "${token0.symbol}-${token1.symbol}",
                breakdown = breakdown,
                tokens = listOf(token0, token1),
                marketSize = refreshable(marketSize) {
                    fiftyFiftyBreakdown(token0, token1, pool.address).sumOf {
                        it.reserveUSD
                    }
                },
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