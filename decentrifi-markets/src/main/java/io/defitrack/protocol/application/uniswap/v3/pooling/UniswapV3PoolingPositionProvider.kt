package io.defitrack.protocol.application.uniswap.v3.pooling

import arrow.core.getOrElse
import io.defitrack.adapter.output.domain.market.GetPriceCommand
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.market.domain.pooling.PoolingPosition
import io.defitrack.market.domain.pooling.PriceCalculator
import io.defitrack.market.port.out.PoolingPositionProvider
import io.defitrack.uniswap.v3.UniswapPositionsV3Contract
import io.defitrack.uniswap.v3.UniswapV3PoolContract
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.BigInteger

abstract class UniswapV3PoolingPositionProvider(
    private val uniswapV3PoolingMarketProvider: UniswapV3PoolingMarketProvider,
) : PoolingPositionProvider() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    val LOG_PRICE = BigDecimal.valueOf(1.0001)
    val POW_96 = BigInteger.valueOf(2).pow(96)

    val poolingNftContract = lazyAsync {
        UniswapPositionsV3Contract(
            gateway(),
            "0xC36442b4a4522E871399CD717aBDD847Ab11FE88"
        )
    }


    override suspend fun fetchUserPoolings(protocol: String, address: String): List<PoolingPosition> = coroutineScope {
        val positionsForUser = poolingNftContract.await().getUserPositions(address)
        positionsForUser.filter {
            it.liquidity > BigInteger.ZERO
        }.map { position ->
            async {
                try {
                    val poolAddress = uniswapV3PoolingMarketProvider.poolFactory.getPool(
                        position.token0,
                        position.token1,
                        position.fee
                    )

                    val poolContract =
                        with(gateway()) {
                            UniswapV3PoolContract(
                                poolAddress
                            )
                        }

                    val market = uniswapV3PoolingMarketProvider.marketFromCache(poolAddress).getOrElse {
                        throw Exception("Market ($poolAddress) not found")
                    }

                    val token0 = uniswapV3PoolingMarketProvider.getToken(poolContract.token0.await())
                    val token1 = uniswapV3PoolingMarketProvider.getToken(poolContract.token1.await())

                    val slot0 = poolContract.slot0.await()
                    val userTokens0 = calculateAmount(
                        position.tickLower,
                        position.liquidity,
                        position.tickUpper,
                        token0.decimals,
                        slot0.tick.toInt(),
                        0
                    )

                    val userTokens1 = calculateAmount(
                        position.tickLower,
                        position.liquidity,
                        position.tickUpper,
                        token1.decimals,
                        slot0.tick.toInt(),
                        1
                    )

                    val totalToken0Usd = if (userTokens0 > BigDecimal.ZERO) {
                        uniswapV3PoolingMarketProvider.getPriceResource()
                            .calculatePrice(
                                GetPriceCommand(
                                    token0.address,
                                    uniswapV3PoolingMarketProvider.getNetwork(),
                                    userTokens0
                                )
                            )
                    } else {
                        0.0
                    }

                    val totalToken1Usd = if (userTokens1 > BigDecimal.ZERO) {
                        uniswapV3PoolingMarketProvider.getPriceResource().calculatePrice(
                            GetPriceCommand(
                                token1.address,
                                uniswapV3PoolingMarketProvider.getNetwork(),
                                userTokens1
                            )
                        )
                    } else {
                        0.0
                    }

                    PoolingPosition(
                        tokenAmount = BigInteger.ZERO,
                        market,
                        object : PriceCalculator {
                            override fun calculate(): Double {
                                return totalToken0Usd + totalToken1Usd
                            }
                        }
                    )
                } catch (ex: Exception) {
                    logger.info(
                        "Unable to fetch claimables for ${uniswapV3PoolingMarketProvider.getNetwork()}", ex.message
                    )
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    private fun gateway() = uniswapV3PoolingMarketProvider.getBlockchainGateway()

    fun calculateAmount(
        tickLower: BigInteger,
        liquidity: BigInteger,
        tickUpper: BigInteger,
        decimals: Int,
        tick: Int,
        tokenPosition: Int
    ): BigDecimal {

        val sqrt = computeSqrtForAmounts(
            tickLower.toInt(),
            tickUpper.toInt(),
            tick
        )

        return if (tokenPosition == 0) {
            (liquidity.toBigDecimal() * POW_96.toBigDecimal() * (sqrt.third.toBigDecimal() - sqrt.first.toBigDecimal()) / (sqrt.third.toBigDecimal() * sqrt.first.toBigDecimal())).asEth(
                decimals
            )
        } else {
            (liquidity.toBigDecimal() * (sqrt.first.toBigDecimal() - sqrt.second.toBigDecimal()) / POW_96.toBigDecimal()).asEth(
                decimals
            )
        }
    }


    fun computeSqrtForAmounts(
        tickLower: Int,
        tickUpper: Int,
        tick: Int
    ): Triple<BigInteger, BigInteger, BigInteger> {
        val sqrtA = (Math.pow(LOG_PRICE.toDouble(), (tickLower / 2).toDouble())).toBigDecimal() * POW_96.toBigDecimal()
        val sqrtB = (Math.pow(LOG_PRICE.toDouble(), (tickUpper / 2).toDouble())).toBigDecimal() * POW_96.toBigDecimal()
        var sqrt = (Math.pow(LOG_PRICE.toDouble(), (tick / 2).toDouble())).toBigDecimal() * POW_96.toBigDecimal()
        sqrt = sqrt.coerceIn(sqrtA, sqrtB)

        return Triple(sqrt.toBigInteger(), sqrtA.toBigInteger(), sqrtB.toBigInteger())
    }
}