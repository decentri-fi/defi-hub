package io.defitrack.protocol.maple.domain

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

class MapleReserve(
    val id: String,
    val underlyingAsset: String,
    val name: String,
    val decimals: Int,
    val symbol: String,
    val liquidityRate: BigInteger,
    val variableBorrowRate: BigInteger,
    val totalLiquidity: BigInteger,
) {
    val lendingRate by lazy {
        liquidityRate.toBigDecimal().divide(BigDecimal.TEN.pow(25), 2, RoundingMode.HALF_UP).toDouble()
    }
    val borrowRate by lazy {
        variableBorrowRate.toBigDecimal().divide(BigDecimal.TEN.pow(25), 2, RoundingMode.HALF_UP).toDouble()
    }
}
