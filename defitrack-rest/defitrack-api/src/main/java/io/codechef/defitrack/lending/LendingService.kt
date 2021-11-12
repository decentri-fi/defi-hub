package io.codechef.defitrack.lending

import io.codechef.defitrack.lending.domain.LendingElement
import io.codechef.defitrack.protocol.ProtocolService
import io.codechef.defitrack.staking.domain.StakingElement

interface LendingService : ProtocolService {
    fun getLendings(address: String): List<LendingElement>

    open fun getLending(address: String, vaultId: String): LendingElement? {
        return getLendings(address).firstOrNull {
            it.id == vaultId
        }
    }
}