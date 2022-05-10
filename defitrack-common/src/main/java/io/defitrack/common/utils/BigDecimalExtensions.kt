package io.defitrack.common.utils

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

object BigDecimalExtensions {

    fun BigDecimal.isZero(): Boolean {
        return this.compareTo(BigDecimal.ZERO) == 0
    }

    fun BigDecimal.dividePrecisely(other: BigDecimal): BigDecimal {
        return this.divide(other, 18, RoundingMode.HALF_UP)
    }

    fun BigInteger.dividePrecisely(other: BigDecimal): BigDecimal {
        return this.toBigDecimal().dividePrecisely(other)
    }
}