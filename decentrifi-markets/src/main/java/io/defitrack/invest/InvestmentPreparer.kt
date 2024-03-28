package io.defitrack.invest

import io.defitrack.common.network.Network
import io.defitrack.domain.BalanceResource
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.port.output.ERC20Client
import io.defitrack.transaction.PreparedTransaction
import io.defitrack.transaction.domain.TransactionPreparationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.math.BigInteger

abstract class InvestmentPreparer(
    private val erC20Resource: ERC20Client,
    private val balanceResource: BalanceResource
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
        return prepareInvestmentCommand.amount ?: balanceResource.getTokenBalance(
            getNetwork(),
            prepareInvestmentCommand.user,
            getWant(),
        ).amount
    }

    suspend fun getAllowanceTransaction(prepareInvestmentCommand: PrepareInvestmentCommand): PreparedTransaction? {
        val allowance = getAllowance(prepareInvestmentCommand)
        val investmentAmount = getInvestmentAmount(prepareInvestmentCommand)
        val balance = balanceResource.getTokenBalance(
            getNetwork(),
            getWant(),
            prepareInvestmentCommand.user
        )

        if (investmentAmount <= BigInteger.ZERO) {
            throw TransactionPreparationException("${prepareInvestmentCommand.user} doesn't own any of the required assets")
        }

        if (balance.amount < investmentAmount) {
            throw TransactionPreparationException("${prepareInvestmentCommand.user} doesn't own enough of the required assets")
        }


        return if (allowance < investmentAmount) {
            PreparedTransaction(
                function = ERC20Contract.fullApprove(getEntryContract()),
                to = getWant(),
                network = getNetwork()
            )
        } else {
            null
        }
    }
}