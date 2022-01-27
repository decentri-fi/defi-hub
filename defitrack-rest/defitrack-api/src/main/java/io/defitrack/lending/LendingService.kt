package io.defitrack.lending

import io.defitrack.lending.domain.LendingElement
import io.defitrack.protocol.ProtocolService

interface LendingService : ProtocolService {
    fun getLendings(address: String): List<LendingElement>

    fun getLending(address: String, vaultId: String): LendingElement? {
        return getLendings(address).firstOrNull {
            it.id == vaultId
        }
    }
}