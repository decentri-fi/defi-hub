package io.defitrack.borrowing

import io.defitrack.borrowing.domain.BorrowElement
import io.defitrack.protocol.ProtocolService

interface BorrowService : ProtocolService {
    fun getBorrows(address: String): List<BorrowElement>
}