package io.defitrack.market.borrowing

import io.defitrack.market.borrowing.domain.BorrowPosition
import io.defitrack.protocol.ProtocolService

interface BorrowService : ProtocolService {
    suspend fun getBorrows(address: String): List<BorrowPosition>
}