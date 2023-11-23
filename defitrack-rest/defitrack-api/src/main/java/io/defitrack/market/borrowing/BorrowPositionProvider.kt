package io.defitrack.market.borrowing

import io.defitrack.market.borrowing.domain.BorrowPosition
import io.defitrack.protocol.ProtocolService

interface BorrowPositionProvider : ProtocolService {
    suspend fun getPositions(address: String): List<BorrowPosition>
}