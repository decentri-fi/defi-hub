package io.defitrack.staking.domain

import io.defitrack.common.network.Network
import io.defitrack.invest.PrepareInvestmentCommand
import io.defitrack.token.ERC20Resource
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

abstract class InvestmentPreparer(private val erC20Resource: ERC20Resource) {
    open suspend fun prepare(prepareInvestmentCommand: PrepareInvestmentCommand): List<PreparedTransaction> {
        return listOf(
            getAllowanceTransaction(prepareInvestmentCommand),
            getInvestmentTransaction(prepareInvestmentCommand)
        ).awaitAll().filterNotNull()
    }

    abstract suspend fun getInvestmentTransaction(prepareInvestmentCommand: PrepareInvestmentCommand): Deferred<PreparedTransaction?>
    abstract fun getToken(): String
    abstract fun getEntryContract(): String

    abstract fun getNetwork(): Network

    fun getAllowance(prepareInvestmentCommand: PrepareInvestmentCommand) =
        erC20Resource.getAllowance(
            getNetwork(),
            getToken(),
            prepareInvestmentCommand.user,
            getEntryContract()
        )


    fun getWantBalance(
        prepareInvestmentCommand: PrepareInvestmentCommand,
    ) =
        prepareInvestmentCommand.amount ?: erC20Resource.getBalance(
            getNetwork(),
            getToken(),
            prepareInvestmentCommand.user
        )

    suspend fun getAllowanceTransaction(prepareInvestmentCommand: PrepareInvestmentCommand): Deferred<PreparedTransaction?> {
        return coroutineScope {
            async {
                val allowance = getAllowance(prepareInvestmentCommand)
                val requiredBalance = getWantBalance(prepareInvestmentCommand)
                if (allowance < requiredBalance) {
                    PreparedTransaction(
                        function = erC20Resource.getFullApproveFunction(
                            getNetwork(),
                            getToken(),
                            getEntryContract()
                        )
                    )
                } else {
                    null
                }
            }
        }
    }
}