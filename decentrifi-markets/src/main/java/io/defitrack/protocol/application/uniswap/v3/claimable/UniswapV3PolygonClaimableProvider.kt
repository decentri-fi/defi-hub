package io.defitrack.protocol.application.uniswap.v3.claimable

import arrow.fx.coroutines.parMapNotNull
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.claim.AbstractUserClaimableProvider
import io.defitrack.claim.UserClaimable
import io.defitrack.common.network.Network
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.application.uniswap.v3.pooling.UniswapV3PolygonPoolingMarketProvider
import io.defitrack.uniswap.v3.UniswapPosition
import io.defitrack.uniswap.v3.UniswapPositionsV3Contract
import io.defitrack.uniswap.v3.UniswapV3PoolContract
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.UNISWAP)
@ConditionalOnProperty(value = ["polygon.enabled", "uniswapv3.enabled"], havingValue = "true", matchIfMissing = true)
class UniswapV3PolygonClaimableProvider(
    private val uniswapV3PoolingMarketProvider: UniswapV3PolygonPoolingMarketProvider,
) : AbstractUserClaimableProvider() {

    val logger = LoggerFactory.getLogger(this::class.java)

    val poolingNftContract by lazy {
        UniswapPositionsV3Contract(
            uniswapV3PoolingMarketProvider.getBlockchainGateway(),
            "0xC36442b4a4522E871399CD717aBDD847Ab11FE88"
        )
    }

    private fun calculateOwed(
        feeGrowthGlobalX128: BigInteger,
        feeGrowthInsideLastX128: BigInteger,
        feeGrowthOutsideLower: BigInteger,
        feeGrowthOutsideUpper: BigInteger,
        liquidity: BigInteger,
    ): BigInteger {
        return (feeGrowthGlobalX128.minus(feeGrowthOutsideLower).minus(feeGrowthOutsideUpper)
            .minus(feeGrowthInsideLastX128))
            .times(liquidity).divide(BigInteger.TWO.pow(128))
    }

    override suspend fun claimables(address: String): List<UserClaimable> = coroutineScope {
        val positionsForUser = poolingNftContract.getUserPositions(address)
        positionsForUser.parMapNotNull(concurrency = 12) { position ->
            val hasYield =
                position.liquidity > BigInteger.ZERO &&
                        (position.feeGrowthInside0LastX128 > BigInteger.ZERO || position.feeGrowthInside1LastX128 > BigInteger.ZERO)

            if (hasYield) {
                transformToClaimables(position, address)
            } else {
                null
            }
        }.flatten()
    }

    private suspend fun transformToClaimables(
        position: UniswapPosition,
        address: String
    ): List<UserClaimable> = coroutineScope {
        try {
            val poolAddress = uniswapV3PoolingMarketProvider.poolFactory.getPool(
                position.token0,
                position.token1,
                position.fee
            )

            val poolContract = with(getBlockchainGateway()) { UniswapV3PoolContract(poolAddress) }

            val token0Async = async { uniswapV3PoolingMarketProvider.getToken(poolContract.token0.await()) }
            val token1Async = async { uniswapV3PoolingMarketProvider.getToken(poolContract.token1.await()) }

            val (upperTicks, lowerTicks) = awaitAll(
                async { poolContract.ticks(position.tickUpper) }, async { poolContract.ticks(position.tickLower) }
            )

            val owedTokens0 = async {
                calculateOwed(
                    poolContract.feeGrowthGlobal0X128.await(),
                    upperTicks.feeGrowthOutside0X128,
                    lowerTicks.feeGrowthOutside0X128,
                    position.feeGrowthInside0LastX128,
                    position.liquidity,
                )
            }

            val owedToken1 = async {
                calculateOwed(
                    poolContract.feeGrowthGlobal1X128.await(),
                    upperTicks.feeGrowthOutside1X128,
                    lowerTicks.feeGrowthOutside1X128,
                    position.feeGrowthInside1LastX128,
                    position.liquidity,
                )
            }

            val token1 = token1Async.await()
            val token0 = token0Async.await()

            listOf(
                UserClaimable(
                    id = "$address-${token1.address}-${token1.address}",
                    name = "${token0.symbol}/${token1.symbol} yield",
                    protocol = getProtocol(),
                    network = getNetwork(),
                    claimableToken = token1,
                    amount = owedTokens0.await(),
                ),
                UserClaimable(
                    id = "$address-${token0.address}-${token1.address}",
                    name = "${token0.symbol}/${token1.symbol} yield",
                    protocol = getProtocol(),
                    network = getNetwork(),
                    claimableToken = token1,
                    amount = owedToken1.await()
                )
            )
        } catch (ex: Exception) {
            logger.debug(ex.message)
            emptyList()
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.UNISWAP_V3
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}