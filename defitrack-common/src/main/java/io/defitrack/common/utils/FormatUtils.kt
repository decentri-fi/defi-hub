package io.defitrack.common.utils

import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.domain.PrettyAmount
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

object FormatUtils {
    fun asEth(weiBalance: BigInteger?): Double {
        return if (weiBalance != null) {
            BigDecimal(weiBalance).divide(
                BigDecimal.valueOf(Math.pow(10.0, 18.0)),
                18,
                RoundingMode.HALF_DOWN
            ).toDouble()
        } else {
            (-1).toDouble()
        }
    }

    fun asEth(weiBalance: BigDecimal?): Double {
        return weiBalance?.divide(
            BigDecimal.valueOf(Math.pow(10.0, 18.0)),
            18,
            RoundingMode.HALF_DOWN
        )?.toDouble()
            ?: (-1).toDouble()
    }


    fun asEth(weiBalance: BigInteger?, decimals: Int): BigDecimal {
        return BigDecimal(weiBalance).dividePrecisely(BigDecimal.TEN.pow(decimals))
    }

    fun asWei(ethBalance: Double): BigInteger {
        return BigInteger.valueOf((ethBalance * Math.pow(10.0, 18.0)).toLong())
    }

    fun asWei(ethBalance: Double, decimals: Int): BigInteger {
        return BigInteger.valueOf((ethBalance * Math.pow(10.0, decimals.toDouble())).toLong())
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