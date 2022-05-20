package io.defitrack.claimable

import io.defitrack.common.network.Network
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll

abstract class ClaimingPreparer() {
    open suspend fun prepare(prepareClaimCommand: PrepareClaimCommand): List<PreparedTransaction> {
        return listOf(
            getClaimPreparation(prepareClaimCommand)
        ).awaitAll().filterNotNull()
    }

    abstract suspend fun getClaimPreparation(prepareInvestmentCommand: PrepareClaimCommand): Deferred<PreparedTransaction?>
    abstract fun getEntryContract(): String
    abstract fun getNetwork(): Network
}