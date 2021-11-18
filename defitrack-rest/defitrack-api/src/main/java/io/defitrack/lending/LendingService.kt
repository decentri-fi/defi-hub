package io.defitrack.lending

import io.defitrack.lending.domain.LendingElement
import io.codechef.defitrack.protocol.ProtocolService

interface LendingService : ProtocolService {
    fun getLendings(address: String): List<LendingElement>

    open fun getLending(address: String, vaultId: String): LendingElement? {
        return getLendings(address).firstOrNull {
            it.id == vaultId
        }
    }
}