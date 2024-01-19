package io.defitrack.market.domain.borrow

import java.math.BigInteger

data class BorrowPosition(
    val underlyingAmount: BigInteger,
    val tokenAmount: BigInteger,
    val market: BorrowMarket
)

