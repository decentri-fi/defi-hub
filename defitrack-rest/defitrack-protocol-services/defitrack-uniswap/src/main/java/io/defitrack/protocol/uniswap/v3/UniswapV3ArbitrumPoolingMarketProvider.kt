package io.defitrack.protocol.uniswap.v3

import com.google.gson.JsonParser
import io.defitrack.abi.TypeUtils
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.token.TokenType
import io.defitrack.uniswap.v3.UniswapV3PoolContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.datatypes.Event
import java.math.BigInteger

//@Component
class UniswapV3ArbitrumPoolingMarketProvider() : PoolingMarketProvider() {

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

    val poolAddresses by lazy {
        runBlocking {
            val gateway = getBlockchainGateway()
            val logs = gateway.getEvents(
                BlockchainGateway.GetEventLogsCommand(
                    addresses = listOf("0x1f98431c8ad98523631ae4a59f267346ea31f984"),
                    topic = "0x783cca1c0412dd0d695e784568c96da2e9c22ff989357a2e8b1d9b2b4e6b7118",
                    fromBlock = BigInteger("165", 10)
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
                            val token = getToken(it)
                            val token0 = getToken(uniswapV3Pool.token0())
                            val token1 = getToken(uniswapV3Pool.token1())
                            val underlyingTokens = listOf(
                                token0,
                                token1
                            )
                            send(
                                create(
                                    identifier = "v3-${it}",
                                    name = "Uniswap V3 ${token0.symbol}/${token1.symbol} LP",
                                    address = it,
                                    symbol = "${token0.symbol}-${token1.symbol}",
                                    breakdown = defaultBreakdown(underlyingTokens, uniswapV3Pool.address),
                                    tokens = underlyingTokens.map { it.toFungibleToken() },
                                    apr = null,
                                    marketSize = marketSizeService.getMarketSize(
                                        underlyingTokens.map { it.toFungibleToken() }, it, getNetwork()
                                    ),
                                    tokenType = TokenType.UNISWAP,
                                    positionFetcher = defaultPositionFetcher(token.address),
                                    totalSupply = token.totalSupply
                                )
                            )
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        logger.error("something went wrong trying to import uniswap market ${it}")
                    }
                }
            }
        }
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}