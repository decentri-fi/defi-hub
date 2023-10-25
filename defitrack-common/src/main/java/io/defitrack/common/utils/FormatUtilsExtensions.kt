package io.defitrack.common.utils

import io.defitrack.common.utils.FormatUtilsExtensions.asWei
import java.math.BigDecimal
import java.math.BigInteger

object FormatUtilsExtensions {
    fun BigInteger.asEth(decimals: Int = 18): BigDecimal = FormatUtils.asEth(this, decimals)
    fun BigInteger.asEth(decimals: BigInteger = BigInteger.valueOf(18)): BigDecimal =
        FormatUtils.asEth(this, decimals.toInt())

    fun BigDecimal.asEth(decimals: Int = 18): BigDecimal = FormatUtils.asEth(this, decimals)
    fun BigDecimal.asWei(decimals: Int = 18): BigInteger = FormatUtils.asWei(this, decimals)
}