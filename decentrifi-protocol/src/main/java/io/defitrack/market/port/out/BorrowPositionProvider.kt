package io.defitrack.market.port.out

import io.defitrack.market.domain.borrow.BorrowPosition
import io.defitrack.protocol.ProtocolService

interface BorrowPositionProvider : ProtocolService {
    suspend fun getPositions(address: String): List<BorrowPosition>
}