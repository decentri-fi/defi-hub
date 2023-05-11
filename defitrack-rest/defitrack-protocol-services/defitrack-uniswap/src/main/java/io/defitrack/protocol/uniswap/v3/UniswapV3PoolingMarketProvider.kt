package io.defitrack.protocol.uniswap.v3

import com.google.gson.JsonParser
import io.defitrack.abi.TypeUtils
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.token.TokenType
import io.defitrack.uniswap.v3.UniswapFactoryContract
import io.defitrack.uniswap.v3.UniswapV3PoolContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.datatypes.Event
import java.math.BigInteger

abstract class UniswapV3PoolingMarketProvider(
    private val fromBlock: String,
    private val poolFactoryAddress: String
) : PoolingMarketProvider() {

    val poolCreatedEvent = Event(
        "PoolCreated",
        listOf(
            TypeUtils.address(true),
            TypeUtils.address(true),
            TypeUtils.uint24(true),
            TypeUtils.int24(),
            TypeUtils.address(false)
        )
    )

    val poolFactory by lazy {
        UniswapFactoryContract(
            getBlockchainGateway(),
            poolFactoryAddress
        )
    }

    val poolAddresses by lazy {
        runBlocking {
            val gateway = getBlockchainGateway()
            val logs = gateway.getEvents(
                BlockchainGateway.GetEventLogsCommand(
                    addresses = listOf(poolFactoryAddress),
                    topic = "0x783cca1c0412dd0d695e784568c96da2e9c22ff989357a2e8b1d9b2b4e6b7118",
                    fromBlock = BigInteger(fromBlock, 10)
                )
            )

            JsonParser().parse(logs).asJsonObject["result"].asJsonArray.map {
                val data = it.asJsonObject["data"].asString
                FunctionReturnDecoder.decode(
                    data, poolCreatedEvent.nonIndexedParameters
                )[1].value as String
            }
        }
    }

    override suspend fun produceMarkets(): Flow<PoolingMarket> {
        return channelFlow {
            poolAddresses.forEach {
                launch {
                    try {
                        throttled {
                            val uniswapV3Pool = UniswapV3PoolContract(
                                getBlockchainGateway(),
                                it
                            )
                            val market = toMarket(uniswapV3Pool)
                            send(market)
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        logger.error("something went wrong trying to import uniswap market ${it}")
                    }
                }
            }
        }
    }

    suspend fun toMarket(pool: UniswapV3PoolContract): PoolingMarket {
        val token = getToken(pool.address)
        val token0 = getToken(pool.token0())
        val token1 = getToken(pool.token1())
        val underlyingTokens = listOf(
            token0,
            token1
        )
        return create(
            identifier = "v3-${pool.address}",
            name = "Uniswap V3 ${token0.symbol}/${token1.symbol} LP",
            address = pool.address,
            symbol = "${token0.symbol}-${token1.symbol}",
            breakdown = defaultBreakdown(underlyingTokens, pool.address),
            tokens = underlyingTokens.map { it.toFungibleToken() },
            apr = null,
            marketSize = marketSizeService.getMarketSize(
                underlyingTokens.map { it.toFungibleToken() }, pool.address, getNetwork()
            ),
            tokenType = TokenType.UNISWAP,
            positionFetcher = null,
            totalSupply = token.totalSupply,
            erc20Compatible = false,
            metadata = mapOf(
                "tick" to pool.slot0().tick
            )
        )
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}