package io.defitrack.claimable

import io.defitrack.common.network.Network
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.Deferred

abstract class ClaimingPreparer() {
    open suspend fun prepare(prepareClaimCommand: PrepareClaimCommand? = null): PreparedTransaction {
        return getClaimPreparation(prepareClaimCommand).await()
    }

    abstract suspend fun getClaimPreparation(prepareInvestmentCommand: PrepareClaimCommand?): Deferred<PreparedTransaction>
    abstract fun getEntryContract(): String
    abstract fun getNetwork(): Network
}