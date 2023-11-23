package io.defitrack.market.farming.domain

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.exception.TransactionPreparationException
import io.defitrack.invest.PrepareInvestmentCommand
import io.defitrack.network.toVO
import io.defitrack.token.ERC20Resource
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.math.BigInteger

abstract class InvestmentPreparer(
    private val erC20Resource: ERC20Resource
) {
    suspend fun prepare(prepareInvestmentCommand: PrepareInvestmentCommand): List<PreparedTransaction> =
        coroutineScope {
            listOf(
                async { getAllowanceTransaction(prepareInvestmentCommand) },
                async {
                    getInvestmentTransaction(
                        prepareInvestmentCommand.user,
                        getInvestmentAmount(prepareInvestmentCommand)
                    )
                }
            ).awaitAll().filterNotNull()
        }

    abstract suspend fun getInvestmentTransaction(user: String, amount: BigInteger): PreparedTransaction
    abstract suspend fun getWant(): String
    abstract fun getEntryContract(): String

    abstract fun getNetwork(): Network

    suspend fun getAllowance(prepareInvestmentCommand: PrepareInvestmentCommand) =
        erC20Resource.getAllowance(
            getNetwork(),
            getWant(),
            prepareInvestmentCommand.user,
            getEntryContract()
        )


    private suspend fun getInvestmentAmount(prepareInvestmentCommand: PrepareInvestmentCommand): BigInteger {
        return prepareInvestmentCommand.amount ?: erC20Resource.getBalance(
            getNetwork(),
            getWant(),
            prepareInvestmentCommand.user
        )
    }

    suspend fun getAllowanceTransaction(prepareInvestmentCommand: PrepareInvestmentCommand): PreparedTransaction? {
        val allowance = getAllowance(prepareInvestmentCommand)
        val investmentAmount = getInvestmentAmount(prepareInvestmentCommand)
        val balance = erC20Resource.getBalance(
            getNetwork(),
            getWant(),
            prepareInvestmentCommand.user
        )


        if (investmentAmount <= BigInteger.ZERO) {
            throw TransactionPreparationException("${prepareInvestmentCommand.user} doesn't own any of the required assets")
        }

        if (balance < investmentAmount) {
            throw TransactionPreparationException("${prepareInvestmentCommand.user} doesn't own enough of the required assets")
        }


        return if (allowance < investmentAmount) {
            PreparedTransaction(
                function = ERC20Contract.fullApprove(getEntryContract()),
                to = getWant(),
                network = getNetwork().toVO()
            )
        } else {
            null
        }
    }
}