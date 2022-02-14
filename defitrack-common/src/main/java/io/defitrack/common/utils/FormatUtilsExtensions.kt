package io.defitrack.common.utils

import java.math.BigDecimal
import java.math.BigInteger

object FormatUtilsExtensions {
    fun BigInteger.asEth(): Double = FormatUtils.asEth(this)
    fun BigDecimal.asEth(): Double = FormatUtils.asEth(this)

    fun BigInteger.asEth(decimals: Int): BigDecimal = FormatUtils.asEth(this, decimals)

    fun Double.asWei(): BigInteger = FormatUtils.asWei(this)

    fun Long.asWei() = BigInteger.valueOf(this).times(BigInteger.TEN.pow(18))

    fun Double.asWei(decimals: Int): BigInteger = FormatUtils.asWei(this, decimals)
}