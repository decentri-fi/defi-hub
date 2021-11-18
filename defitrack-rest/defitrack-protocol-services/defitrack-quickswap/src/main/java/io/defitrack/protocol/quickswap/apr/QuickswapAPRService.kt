package io.defitrack.protocol.quickswap.apr

import io.defitrack.abi.ABIResource
import io.defitrack.abi.PriceResource
import io.defitrack.polygon.config.PolygonContractAccessor
import io.defitrack.protocol.quickswap.QuickswapDualRewardPoolContract
import io.defitrack.protocol.quickswap.QuickswapRewardPoolContract
import io.defitrack.quickswap.QuickswapService
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@Component
class QuickswapAPRService(
    private val quickswapService: QuickswapService,
    private val abiResource: ABIResource,
    private val polygonContractAccessor: PolygonContractAccessor,
    private val priceResource: PriceResource,
) {

    val stakingRewardsABI by lazy {
        abiResource.getABI("quickswap/StakingRewards.json")
    }

    val stakingDualRewards by lazy {
        abiResource.getABI("quickswap/DualStakingRewards.json")
    }

    @OptIn(ExperimentalTime::class)
    val cache = Cache.Builder().expireAfterWrite(
        Duration.Companion.hours(1)
    ).build<String, BigDecimal>()

    fun getDualPoolAPR(address: String): BigDecimal {
        return runBlocking {
            cache.get("dual-rewardpool-$address") {
                calculateDualRewardPool(address)
            }
        }
    }

    fun getRewardPoolAPR(address: String): BigDecimal {
        return runBlocking {
            cache.get("rewardpool-$address") {
                calculateSingleRewardPool(address)
            }
        }
    }

    private fun calculateDualRewardPool(address: String): BigDecimal {
        val contract = QuickswapDualRewardPoolContract(
            polygonContractAccessor,
            stakingDualRewards,
            address
        )
        val quickRewardsPerYear =
            (contract.rewardRateA.times(BigInteger.valueOf(31536000))).toBigDecimal()
                .divide(BigDecimal.TEN.pow(18))
        val usdQuickRewardsPerYear = priceResource.getPrice("QUICK").times(
            quickRewardsPerYear
        )

        val maticRewardsPerYear =
            (contract.rewardRateB.times(BigInteger.valueOf(31536000))).toBigDecimal()
                .divide(BigDecimal.TEN.pow(18))
        val usdMaticRewardsPerYear = priceResource.getPrice("MATIC").times(
            maticRewardsPerYear
        )

        val reserveUsd = quickswapService.getPairs().find {
            it.id.lowercase() == contract.stakingTokenAddress
        }?.reserveUSD ?: BigDecimal.ZERO

        return if ((usdQuickRewardsPerYear == BigDecimal.ZERO && usdMaticRewardsPerYear == BigDecimal.ZERO) || reserveUsd == BigDecimal.ZERO) {
            BigDecimal.ZERO
        } else {
            (usdQuickRewardsPerYear.plus(usdMaticRewardsPerYear)).divide(reserveUsd, 6, RoundingMode.HALF_UP)
        }
    }

    private fun calculateSingleRewardPool(address: String): BigDecimal {
        val contract = QuickswapRewardPoolContract(
            polygonContractAccessor,
            stakingRewardsABI,
            address
        )

        val quickRewardsPerYear =
            (contract.rewardRate.times(BigInteger.valueOf(31536000))).toBigDecimal()
                .divide(BigDecimal.TEN.pow(18))
        val usdRewardsPerYear = priceResource.getPrice("DQUICK").times(
            quickRewardsPerYear
        )

        val reserveUsd = quickswapService.getPairs().find {
            it.id.lowercase() == contract.stakingTokenAddress
        }?.reserveUSD ?: BigDecimal.ZERO

        return if (usdRewardsPerYear == BigDecimal.ZERO || reserveUsd == BigDecimal.ZERO) {
            BigDecimal.ZERO
        } else {
            usdRewardsPerYear.divide(reserveUsd, 4, RoundingMode.HALF_UP)
        }
    }

    @Cacheable(cacheNames = ["quickswap-aprs"], key = "'lp-' + #address")
    fun getLPAPR(address: String): BigDecimal {
        try {
            val pairData = quickswapService.getPairDayData(address)
            return if (pairData.size <= 1) {
                BigDecimal.ZERO
            } else {
                pairData.drop(1).map {
                    it.dailyVolumeUSD
                }.reduce { a, b -> a.plus(b) }
                    .times(BigDecimal.valueOf(0.003)).times(BigDecimal.valueOf(52))
                    .divide(
                        quickswapService.getPairs().find {
                            it.id == address
                        }!!.reserveUSD,
                        18,
                        RoundingMode.HALF_UP
                    )
            }
        } catch (ex: Exception) {
            return BigDecimal.ZERO
        }
    }
}