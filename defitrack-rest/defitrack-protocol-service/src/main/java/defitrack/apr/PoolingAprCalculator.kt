package io.defitrack.apr

import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.BigDecimalExtensions.isZero
import io.github.reactivecircus.cache4k.Cache
import java.math.BigDecimal
import kotlin.time.Duration.Companion.hours

abstract class PoolingAprCalculator {

    private val cache = Cache.Builder<String, BigDecimal>().expireAfterWrite(10.hours).build()

    suspend fun calculateApr(): BigDecimal  {
        return cache.get("apr") {
            val yearlyRewards = getYearlyRewards()
            val tvl = getTvl()
            if (yearlyRewards.isZero() || tvl.isZero()) {
                BigDecimal.ZERO
            } else {
                yearlyRewards.dividePrecisely(tvl)
            }
        }
    }

    abstract suspend fun getYearlyRewards(): BigDecimal
    abstract suspend fun getTvl(): BigDecimal
}