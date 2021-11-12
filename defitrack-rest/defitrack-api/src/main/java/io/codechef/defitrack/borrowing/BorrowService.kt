package io.codechef.defitrack.borrowing

import io.codechef.defitrack.borrowing.domain.BorrowElement
import io.codechef.defitrack.protocol.ProtocolService

interface BorrowService : ProtocolService {
    fun getBorrows(address: String): List<BorrowElement>
}