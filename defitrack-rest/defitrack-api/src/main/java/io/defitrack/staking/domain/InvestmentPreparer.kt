package io.defitrack.staking.domain

import io.defitrack.common.network.Network
import io.defitrack.exception.TransactionPreparationException
import io.defitrack.invest.PrepareInvestmentCommand
import io.defitrack.token.ERC20Resource
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.math.BigInteger

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


    fun getInvestmentAmount(
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
                val investmentAmount = getInvestmentAmount(prepareInvestmentCommand)
                val balance = erC20Resource.getBalance(
                    getNetwork(),
                    getToken(),
                    prepareInvestmentCommand.user
                )


                if (investmentAmount <= BigInteger.ZERO) {
                    throw TransactionPreparationException("${prepareInvestmentCommand.user} doesn't own any of the required assets")
                }

                if (balance < investmentAmount) {
                    throw TransactionPreparationException("${prepareInvestmentCommand.user} doesn't own enough of the required assets")
                }


                if (allowance < investmentAmount) {
                    PreparedTransaction(
                        function = erC20Resource.getFullApproveFunction(
                            getNetwork(),
                            getToken(),
                            getEntryContract()
                        ),
                        to = getEntryContract()
                    )
                } else {
                    null
                }
            }
        }
    }
}