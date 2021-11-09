package io.codechef.defitrack

import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class APRToAPYConverter {

    fun convert(percentage: BigDecimal, periods: Int): BigDecimal {
        val yieldPerPeriod =
            percentage.divide(BigDecimal.valueOf(periods.toDouble()), 18, RoundingMode.HALF_UP).add(BigDecimal.ONE)
        return (yieldPerPeriod.pow(periods)).min(BigDecimal.ONE)
    }
}