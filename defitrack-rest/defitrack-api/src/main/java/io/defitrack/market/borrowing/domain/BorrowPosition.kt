package io.defitrack.market.borrowing.domain

import java.math.BigInteger

data class BorrowPosition(
    val underlyingAmount: BigInteger,
    val tokenAmount: BigInteger,
    val market: BorrowMarket
)

