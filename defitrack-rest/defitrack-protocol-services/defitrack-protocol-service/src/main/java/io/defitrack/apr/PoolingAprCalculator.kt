package io.defitrack.apr

import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.time.Duration.Companion.hours

abstract class PoolingAprCalculator {

    private val cache = Cache.Builder().expireAfterWrite(10.hours).build<String, BigDecimal>()

    fun calculateApr(): BigDecimal = runBlocking {
        cache.get("apr") {
            val yearlyRewards = getYearlyRewards()
            val tvl = getTvl()
            if (yearlyRewards == BigDecimal.ZERO || tvl == BigDecimal.ZERO) {
                BigDecimal.ZERO
            } else {
                yearlyRewards.divide(tvl, 18, RoundingMode.HALF_UP)
            }
        }
    }

    abstract fun getYearlyRewards(): BigDecimal
    abstract fun getTvl(): BigDecimal
}