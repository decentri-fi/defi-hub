package io.defitrack.protocol.sushiswap.apr

import io.defitrack.common.network.Network
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.SushiswapService
import io.defitrack.protocol.reward.MiniChefV2Contract
import io.defitrack.protocol.staking.TokenType
import io.defitrack.token.ERC20Resource
import io.github.reactivecircus.cache4k.Cache
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

@Component
class SushiswapAPRService(
    private val sushiServices: List<SushiswapService>,
    private val priceResource: PriceResource,
    private val erC20Resource: ERC20Resource
) {

    val secondsPerYear = 60 * 60 * 24 * 365


    @OptIn(ExperimentalTime::class)
    val cache = Cache.Builder().expireAfterWrite(10.hours).build<String, BigDecimal>()

    suspend fun getStakingAPR(
        chef: MiniChefV2Contract,
        poolId: Int
    ): BigDecimal {

        val network = chef.ethereumContractAccessor.getNetwork()

        val poolInfo = chef.poolInfo(poolId);
        val allSushiPerSecond = chef.sushiPerSecond
        val poolBlockRewards = allSushiPerSecond.times(poolInfo.allocPoint).divide(chef.totalAllocPoint)

        val sushiPerYear = poolBlockRewards.times(secondsPerYear.toBigInteger())
        val rewardsPerYearInUsd = priceResource.calculatePrice(
            PriceRequest(
                chef.rewardToken,
                network,
                sushiPerYear.toBigDecimal().divide(BigDecimal.TEN.pow(18), 18, RoundingMode.HALF_UP),
                TokenType.SINGLE
            )
        )
        val stakedToken = chef.getLpTokenForPoolId(poolId)

        val stakedTokenAmount = erC20Resource.getBalance(network, stakedToken, chef.address)
        val totalStakedTokenInUsd = priceResource.calculatePrice(
            PriceRequest(
                stakedToken,
                network,
                stakedTokenAmount.toBigDecimal().divide(BigDecimal.TEN.pow(18), 18, RoundingMode.HALF_UP),
                TokenType.SUSHISWAP
            )
        )

        return rewardsPerYearInUsd.toBigDecimal().divide(totalStakedTokenInUsd.toBigDecimal(), 18, RoundingMode.HALF_UP)
    }

    suspend fun getPoolingAPR(address: String, network: Network): BigDecimal {
        return cache.get("$address-${network.slug}") {
            try {
                val pairData = getSushiswapService(network).getPairDayData(address)
                if (pairData.size <= 1) {
                    BigDecimal.ZERO
                } else {
                    pairData.drop(1).map {
                        it.volumeUSD
                    }.reduce { a, b -> a.plus(b) }
                        .times(BigDecimal.valueOf(0.0025)).times(BigDecimal.valueOf(52))
                        .divide(
                            getSushiswapService(network).getPairs().find {
                                it.id == address
                            }!!.reserveUSD,
                            18,
                            RoundingMode.HALF_UP
                        )
                }
            } catch (ex: Exception) {
                BigDecimal.ZERO
            }
        }
    }

    fun getSushiswapService(network: Network): SushiswapService {
        return sushiServices.find {
            it.getNetwork() == network
        } ?: throw IllegalArgumentException("Not found")
    }
}