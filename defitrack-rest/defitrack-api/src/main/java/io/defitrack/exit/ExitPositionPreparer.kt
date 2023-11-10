package io.defitrack.exit

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.EvmContract
import io.defitrack.transaction.PreparedTransaction
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

abstract class ExitPositionPreparer {

    companion object {
        fun defaultExitPositionProvider(network: Network, contractCallProvider: () -> EvmContract.ContractCall) =
            object : ExitPositionPreparer() {
                override suspend fun getExitPositionCommand(exitPositionCommand: ExitPositionCommand): Deferred<PreparedTransaction> = coroutineScope {
                    async {
                        selfExecutingTransaction(contractCallProvider)
                            .invoke(exitPositionCommand.user)
                    }
                }

                override fun getNetwork(): Network {
                    return network
                }
            }
    }

    open suspend fun prepare(exitPositionCommand: ExitPositionCommand): List<PreparedTransaction> {
        return listOf(
            getExitPositionCommand(exitPositionCommand)
        ).awaitAll().filterNotNull()
    }

    abstract suspend fun getExitPositionCommand(exitPositionCommand: ExitPositionCommand): Deferred<PreparedTransaction?>

    abstract fun getNetwork(): Network
}