package io.defitrack.market.port.`in`

import io.defitrack.market.domain.borrow.BorrowPosition

interface BorrowPositions {
    suspend fun getPositions(protocol: String, address: String): List<BorrowPosition>
}