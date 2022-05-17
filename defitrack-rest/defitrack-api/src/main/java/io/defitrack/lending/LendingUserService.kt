package io.defitrack.lending

import io.defitrack.lending.domain.LendingElement
import io.defitrack.protocol.ProtocolService
import kotlinx.coroutines.runBlocking

interface LendingUserService : ProtocolService {
    suspend fun getLendings(address: String): List<LendingElement>

    fun getLending(address: String, vaultId: String): LendingElement? = runBlocking {
        getLendings(address).firstOrNull {
            it.id == vaultId
        }
    }
}