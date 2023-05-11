package io.defitrack.protocol.uniswap.v3

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.pooling.PoolingPositionProvider
import io.defitrack.market.pooling.domain.PoolingPosition
import io.defitrack.market.pooling.domain.PriceCalculator
import io.defitrack.price.PriceRequest
import io.defitrack.token.ERC20Resource
import io.defitrack.uniswap.v3.UniswapFactoryContract
import io.defitrack.uniswap.v3.UniswapPositionsV3Contract
import io.defitrack.uniswap.v3.UniswapV3PoolContract
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger

@Component
class UniswapV3ArbitrumPoolingPositionProvider(
    private val uniswapV3ArbitrumPoolingMarketProvider: UniswapV3ArbitrumPoolingMarketProvider,
    blockchainGatewayProvider: BlockchainGatewayProvider,
    val erC20Resource: ERC20Resource
) : PoolingPositionProvider() {

    val blockchainGateway = blockchainGatewayProvider.getGateway(Network.ARBITRUM)

    val LOG_PRICE = BigDecimal.valueOf(1.0001)
    val POW_96 = BigInteger.valueOf(2).pow(96)

    val poolingNftContract by lazy {
        UniswapPositionsV3Contract(
            blockchainGateway,
            "0xC36442b4a4522E871399CD717aBDD847Ab11FE88"
        )
    }

    val poolFactory by lazy {
        UniswapFactoryContract(
            blockchainGateway,
            "0x1f98431c8ad98523631ae4a59f267346ea31f984"
        )
    }

    override suspend fun fetchUserPoolings(address: String): List<PoolingPosition> = coroutineScope {
        val positionsForUser = poolingNftContract.getUserPositions(address)
        positionsForUser.filter {
            it.liquidity > BigInteger.ZERO
        }.map { position ->
            async {
                try {
                    val poolAddress = poolFactory.getPool(position.token0, position.token1, position.fee)

                    val poolContract = UniswapV3PoolContract(
                        blockchainGateway,
                        poolAddress
                    )

                    val token0 = erC20Resource.getTokenInformation(Network.ARBITRUM, poolContract.token0())
                    val token1 = erC20Resource.getTokenInformation(Network.ARBITRUM, poolContract.token1())

                    val userTokens0 = calculateAmount(
                        position.tickLower,
                        position.liquidity,
                        position.tickUpper,
                        token0.decimals,
                        poolContract.slot0().tick.toInt(),
                        0
                    )

                    val userTokens1 = calculateAmount(
                        position.tickLower,
                        position.liquidity,
                        position.tickUpper,
                        token1.decimals,
                        poolContract.slot0().tick.toInt(),
                        1
                    )

                    val market = uniswapV3ArbitrumPoolingMarketProvider.toMarket(poolContract)

                    val totalToken0Usd = if (userTokens0 > BigDecimal.ZERO) {
                        uniswapV3ArbitrumPoolingMarketProvider.getPriceResource()
                            .calculatePrice(
                                PriceRequest(
                                    token0.address,
                                    Network.ARBITRUM,
                                    userTokens0
                                )
                            )
                    } else {
                        0.0
                    }

                    val totalToken1Usd = if (userTokens1 > BigDecimal.ZERO) {
                        uniswapV3ArbitrumPoolingMarketProvider.getPriceResource().calculatePrice(
                            PriceRequest(
                                token1.address,
                                Network.ARBITRUM,
                                userTokens1
                            )
                        )
                    } else {
                        0.0
                    }

                    println("got a total of ${userTokens0} ${token0.symbol} (${totalToken0Usd}) and ${userTokens1} ${token1.symbol} ${totalToken1Usd}")

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
                    ex.printStackTrace()
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

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