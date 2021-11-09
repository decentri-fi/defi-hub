package io.codechef.common.utils

import java.math.BigDecimal
import io.codechef.common.utils.domain.PrettyAmount
import io.codechef.common.utils.FormatUtils
import org.web3j.utils.Convert
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


    fun asEth(weiBalance: BigInteger?, decimals: Int): Double {
        return BigDecimal(weiBalance).divide(
            BigDecimal.valueOf(Math.pow(10.0, decimals.toDouble())),
            8,
            RoundingMode.HALF_DOWN
        ).toDouble()
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

    @JvmStatic
    fun prettifyAddress(address: String): String {
        return if (!address.startsWith("0x")) {
            String.format("0x%s", address)
        } else {
            address
        }
    }

    fun isSmallerThan(
        aBigInt: BigInteger,
        `val`: BigInteger?
    ): Boolean {
        return aBigInt.compareTo(`val`) < 0
    }
}