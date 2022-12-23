package io.defitrack.common.utils

import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.domain.PrettyAmount
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger

object FormatUtils {

    fun asEth(weiBalance: BigInteger, decimals: Int = 18): BigDecimal {
        return asEth(weiBalance.toBigDecimal(), decimals)
    }

    fun asEth(weiBalance: BigDecimal, decimals: Int = 18): BigDecimal {
        return weiBalance.dividePrecisely(BigDecimal.TEN.pow(decimals))
    }

    fun asWei(ethBalance: BigDecimal, decimals: Int = 18): BigInteger {
        return ethBalance.times(
            BigDecimal.TEN.pow(decimals)
        ).toBigInteger()
    }

    fun format(value: BigInteger): PrettyAmount {
        return if (value == BigInteger.ZERO) {
            PrettyAmount("0", "wei")
        } else if (isSmallerThan(
                value,
                Convert.toWei("0.01", Convert.Unit.ETHER).toBigInteger()
            )
        ) {
            if (isSmallerThan(
                    value,
                    Convert.toWei("0.01", Convert.Unit.FINNEY).toBigInteger()
                )
            ) {
                if (isSmallerThan(
                        value,
                        Convert.toWei("0.01", Convert.Unit.GWEI).toBigInteger()
                    )
                ) {
                    PrettyAmount(value.toString(), "wei")
                } else {
                    PrettyAmount(
                        Convert.fromWei(BigDecimal(value), Convert.Unit.GWEI)
                            .toPlainString(),
                        "gwei"
                    )
                }
            } else {
                PrettyAmount(
                    Convert.fromWei(BigDecimal(value), Convert.Unit.FINNEY).toPlainString(),
                    "finney"
                )
            }
        } else {
            PrettyAmount(
                Convert.fromWei(BigDecimal(value), Convert.Unit.ETHER).toPlainString(),
                "ether"
            )
        }
    }

    private fun isSmallerThan(
        aBigInt: BigInteger,
        `val`: BigInteger?
    ): Boolean {
        return aBigInt.compareTo(`val`) < 0
    }
}