package io.defitrack.apr

import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.time.Duration.Companion.hours

abstract class AprCalculator(
    private val priceResource: PriceResource,
) {

    private val secondsPerYear = BigDecimal.valueOf(60 * 60 * 24 * 365)
    private val cache = Cache.Builder().expireAfterWrite(1.hours).build<String, BigDecimal>()

    fun calculateApr(): BigDecimal = runBlocking {
        cache.get("apr") {
            val rewardsPerYear = getRewardsPerSecond().sumOf {
                calculateRewardsPerYearInUsd(it)
            }

            val fullyStaked = getStakedTokens().sumOf {
                calculateStakedTokenInUsd(it)
            }

            if (rewardsPerYear == BigDecimal.ZERO || fullyStaked == BigDecimal.ZERO) {
                BigDecimal.ZERO
            } else {
                rewardsPerYear.divide(fullyStaked, 18, RoundingMode.HALF_UP)
            }
        }
    }

    abstract fun getRewardsPerSecond(): List<Reward>
    abstract fun getStakedTokens(): List<StakedAsset>

    fun calculateStakedTokenInUsd(stakedAsset: StakedAsset): BigDecimal = BigDecimal.valueOf(
        priceResource.calculatePrice(
            PriceRequest(
                stakedAsset.address,
                stakedAsset.network,
                stakedAsset.amount,
                stakedAsset.tokenType
            )
        )
    )

    fun calculateRewardsPerYearInUsd(reward: Reward): BigDecimal {
        val amount = reward.amount.times(secondsPerYear)
            .divide(BigDecimal.valueOf(reward.blocksPerSecond.toLong()), 18, RoundingMode.HALF_UP)
        return BigDecimal.valueOf(
            priceResource.calculatePrice(
                PriceRequest(
                    reward.address,
                    reward.network,
                    amount,
                    reward.tokenType
                )
            )
        )
    }
}