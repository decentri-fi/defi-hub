package io.defitrack.protocol.aave.v2.domain

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

class AaveReserve(
    val id: String,
    val underlyingAsset: String,
    val name: String,
    val decimals: Int,
    val symbol: String,
    val liquidityRate: BigInteger,
    val variableBorrowRate: BigInteger,
    val totalLiquidity: BigInteger,
    val aToken: AToken
) {
    val lendingRate by lazy {
        liquidityRate.toBigDecimal().divide(BigDecimal.TEN.pow(25), 6, RoundingMode.HALF_UP).toDouble()
    }
    val borrowRate by lazy {
        variableBorrowRate.toBigDecimal().divide(BigDecimal.TEN.pow(25), 6, RoundingMode.HALF_UP).toDouble()
    }

    class AToken(val id: String)
}