package io.defitrack.apr

import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.BigDecimalExtensions.isZero
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.github.reactivecircus.cache4k.Cache
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.time.Duration.Companion.hours

abstract class StakingAprCalculator(
    private val priceResource: PriceResource,
) {

    private val secondsPerYear = BigDecimal.valueOf(60 * 60 * 24 * 365)
    private val cache = Cache.Builder<String, BigDecimal>().expireAfterWrite(1.hours).build()

    suspend fun calculateApr(): BigDecimal {
        return cache.get("apr") {
            val rewardsPerYear = getRewardsPerSecond().sumOf {
                calculateRewardsPerYearInUsd(it)
            }

            val fullyStaked = getStakedTokens().sumOf {
                calculateStakedTokenInUsd(it)
            }

            if (rewardsPerYear.isZero() || fullyStaked.isZero()) {
                BigDecimal.ZERO
            } else {
                rewardsPerYear.dividePrecisely(fullyStaked)
            }
        }
    }

    abstract suspend fun getRewardsPerSecond(): List<Reward>
    abstract suspend fun getStakedTokens(): List<StakedAsset>

    private suspend fun calculateStakedTokenInUsd(stakedAsset: StakedAsset): BigDecimal = BigDecimal.valueOf(
        priceResource.calculatePrice(
            PriceRequest(
                stakedAsset.address,
                stakedAsset.network,
                stakedAsset.amount,
                stakedAsset.tokenType
            )
        )
    )

    private suspend fun calculateRewardsPerYearInUsd(reward: Reward): BigDecimal {
        return if (reward.blocksPerSecond > 0) {
            val amount = reward.amount.times(secondsPerYear)
                .divide(BigDecimal.valueOf(reward.blocksPerSecond.toLong()), 18, RoundingMode.HALF_UP)
            BigDecimal.valueOf(
                priceResource.calculatePrice(
                    PriceRequest(
                        reward.address,
                        reward.network,
                        amount,
                        reward.tokenType
                    )
                )
            )
        } else BigDecimal.ZERO
    }
}