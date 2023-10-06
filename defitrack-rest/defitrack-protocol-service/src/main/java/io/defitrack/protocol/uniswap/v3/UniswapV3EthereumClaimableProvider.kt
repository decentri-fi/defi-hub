package io.defitrack.protocol.uniswap.v3

import io.defitrack.claimable.UserClaimable
import io.defitrack.claimable.UserClaimableProvider
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
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
@ConditionalOnProperty(value = ["ethereum.enabled", "uniswapv3.enabled"], havingValue = "true", matchIfMissing = true)
class UniswapV3EthereumClaimableProvider(
    private val uniswapV3PoolingMarketProvider: UniswapV3EthereumPoolingMarketProvider,
) : UserClaimableProvider() {

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
        positionsForUser.map { position ->
            val hasYield =
                position.liquidity > BigInteger.ZERO &&
                        (position.feeGrowthInside0LastX128 > BigInteger.ZERO || position.feeGrowthInside1LastX128 > BigInteger.ZERO)

            if (hasYield) {
                async {
                    transformToClaimables(position, address)
                }
            } else {
                null
            }
        }.filterNotNull().awaitAll().flatten()
    }

    private suspend fun transformToClaimables(
        position: UniswapPosition,
        address: String
    ): List<UserClaimable> = coroutineScope {
        try {
            val poolAddress = uniswapV3PoolingMarketProvider.poolFactory.await().getPool(
                position.token0,
                position.token1,
                position.fee
            )
            val market = uniswapV3PoolingMarketProvider.getMarket(poolAddress)


            val poolContract = market.internalMetadata["contract"] as UniswapV3PoolContract

            val token0Async = async { uniswapV3PoolingMarketProvider.getToken(poolContract.token0.await()) }
            val token1Async = async { uniswapV3PoolingMarketProvider.getToken(poolContract.token1.await()) }

            val (upperTicks, lowerTicks) = awaitAll(
                async { poolContract.ticks(position.tickUpper) }, async { poolContract.ticks(position.tickLower) }
            )

            val owedTokens0 = calculateOwed(
                poolContract.feeGrowthGlobal0X128.await(),
                upperTicks.feeGrowthOutside0X128,
                lowerTicks.feeGrowthOutside0X128,
                position.feeGrowthInside0LastX128,
                position.liquidity,
            )

            val owedToken1 = calculateOwed(
                poolContract.feeGrowthGlobal1X128.await(),
                upperTicks.feeGrowthOutside1X128,
                lowerTicks.feeGrowthOutside1X128,
                position.feeGrowthInside1LastX128,
                position.liquidity,
            )

            val token1 = token1Async.await()
            val token0 = token0Async.await()

            listOf(
                UserClaimable(
                    id = "$address-${token1.address}-${token1.address}",
                    name = market.name + " Yield",
                    protocol = getProtocol(),
                    network = getNetwork(),
                    claimableToken = token1.toFungibleToken(),
                    amount = owedTokens0,
                ),
                UserClaimable(
                    id = "$address-${token0.address}-${token1.address}",
                    name = market.name + " Yield",
                    protocol = getProtocol(),
                    network = getNetwork(),
                    claimableToken = token1.toFungibleToken(),
                    amount = owedToken1
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
        return Network.ETHEREUM
    }


}