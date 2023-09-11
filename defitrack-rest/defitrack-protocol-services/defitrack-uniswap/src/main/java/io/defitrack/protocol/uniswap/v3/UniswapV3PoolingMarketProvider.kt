package io.defitrack.protocol.uniswap.v3

import com.google.gson.JsonParser
import io.defitrack.abi.TypeUtils
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.event.EventDecoder.Companion.extract
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.token.TokenType
import io.defitrack.uniswap.v3.UniswapFactoryContract
import io.defitrack.uniswap.v3.UniswapV3PoolContract
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.datatypes.Event
import java.math.BigInteger
import kotlin.time.Duration.Companion.hours

abstract class UniswapV3PoolingMarketProvider(
    private val fromBlocks: List<String>, private val poolFactoryAddress: String
) : PoolingMarketProvider() {

    val poolCreatedEvent = Event(
        "PoolCreated", listOf(
            TypeUtils.address(true),
            TypeUtils.address(true),
            TypeUtils.uint24(true),
            TypeUtils.int24(),
            TypeUtils.address(false)
        )
    )

    val poolFactory = lazyAsync {
        UniswapFactoryContract(
            getBlockchainGateway(), poolFactoryAddress
        )
    }

    val poolAddresses = lazyAsync {
        fromBlocks.mapIndexed { index, block ->
            getLogsBetweenBlocks(block, fromBlocks.getOrNull(index + 1))
        }.flatten()
    }

    suspend fun getLogsBetweenBlocks(fromBlock: String, toBlock: String?): List<String> {
        val gateway = getBlockchainGateway()
        val logs = gateway.getEventsAsEthLog(
            BlockchainGateway.GetEventLogsCommand(
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

    override suspend fun produceMarkets(): Flow<PoolingMarket> {
        return channelFlow {
            poolAddresses.await().forEach {
                launch {
                    try {
                        throttled {
                            send(
                                getMarket(
                                    UniswapV3PoolContract(getBlockchainGateway(), it)
                                )
                            )
                        }
                    } catch (ex: Exception) {
                        logger.error("something went wrong trying to import uniswap market ${it}", ex)
                    }
                }
            }
        }
    }


    val uniswapPoolCache = Cache.Builder<String, PoolingMarket>()
        .expireAfterWrite(4.hours)
        .build()

    suspend fun getMarket(pool: UniswapV3PoolContract): PoolingMarket = uniswapPoolCache.get(pool.address) {
        val token = getToken(pool.address)
        val token0 = getToken(pool.token0())
        val token1 = getToken(pool.token1())

        val underlyingTokens = listOf(
            token0, token1
        )
        val breakdown = defaultBreakdown(underlyingTokens, pool.address)

        create(
            identifier = "v3-${pool.address}",
            name = "${token0.symbol}/${token1.symbol}",
            address = pool.address,
            symbol = "${token0.symbol}-${token1.symbol}",
            breakdown = breakdown,
            tokens = underlyingTokens.map { it.toFungibleToken() },
            marketSize = refreshable(breakdown.sumOf {
                it.reserveUSD
            }) {
                defaultBreakdown(underlyingTokens, pool.address).sumOf {
                    it.reserveUSD
                }
            },
            tokenType = TokenType.UNISWAP,
            positionFetcher = null,
            totalSupply = refreshable(token.totalSupply.asEth(token.decimals)) {
                getToken(pool.address).totalSupply.asEth(token.decimals)
            },
            erc20Compatible = false
        )
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }

    override fun getProtocol(): Protocol {
        return Protocol.UNISWAP_V3
    }
}