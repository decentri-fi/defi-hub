package io.defitrack.common.utils

import java.math.BigDecimal
import java.math.BigInteger

object FormatUtilsExtensions {
    fun BigInteger.asEth(decimals: Int = 18): BigDecimal = FormatUtils.asEth(this, decimals)
    fun BigInteger.asEth(decimals: BigInteger): BigDecimal =
        FormatUtils.asEth(this, decimals.toInt())

    fun BigDecimal.asEth(decimals: Int = 18): BigDecimal = FormatUtils.asEth(this, decimals)
}