package io.defitrack.market.lending.domain

import java.math.BigInteger

class Position(
    val underlyingAmount: BigInteger,
    val tokenAmount: BigInteger
) {
    companion object {
        val ZERO = Position(BigInteger.ZERO, BigInteger.ZERO)
    }
}