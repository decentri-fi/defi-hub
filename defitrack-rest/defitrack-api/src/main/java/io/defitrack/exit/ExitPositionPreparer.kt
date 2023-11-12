package io.defitrack.exit

import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll

abstract class ExitPositionPreparer {

    open suspend fun prepare(exitPositionCommand: ExitPositionCommand): List<PreparedTransaction> {
        return listOf(
            getExitPositionCommand(exitPositionCommand)
        ).awaitAll().filterNotNull()
    }

    abstract suspend fun getExitPositionCommand(exitPositionCommand: ExitPositionCommand): Deferred<PreparedTransaction?>
}