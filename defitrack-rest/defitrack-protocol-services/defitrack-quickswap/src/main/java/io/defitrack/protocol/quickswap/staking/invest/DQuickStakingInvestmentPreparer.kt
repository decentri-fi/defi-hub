package io.defitrack.protocol.quickswap.staking.invest

import io.defitrack.protocol.quickswap.contract.DQuickContract
import io.defitrack.staking.command.PrepareInvestmentCommand
import io.defitrack.staking.domain.InvestmentPreparer
import io.defitrack.token.ERC20Resource
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class DQuickStakingInvestmentPreparer(
    val erC20Resource: ERC20Resource,
    val dQuickContract: DQuickContract
) : InvestmentPreparer {

    val quick = "0x831753dd7087cac61ab5644b308642cc1c33dc13"

    override suspend fun prepare(prepareInvestmentCommand: PrepareInvestmentCommand): List<PreparedTransaction> {
        return listOf(
            getAllowanceTransaction(prepareInvestmentCommand),
            getInvestmentTransaction(prepareInvestmentCommand)
        ).awaitAll().filterNotNull()
    }


    suspend fun getAllowanceTransaction(prepareInvestmentCommand: PrepareInvestmentCommand): Deferred<PreparedTransaction?> =
        coroutineScope {
            async {
                val allowance = getAllowance(prepareInvestmentCommand)
                val requiredBalance = getWantBalance(prepareInvestmentCommand)
                if (allowance < requiredBalance) {
                    PreparedTransaction(
                        function = erC20Resource.getFullApproveFunction(
                            dQuickContract.blockchainGateway.network,
                            quick,
                            dQuickContract.address
                        )
                    )
                } else {
                    null
                }
            }
        }

    private fun getWantBalance(prepareInvestmentCommand: PrepareInvestmentCommand) =
        prepareInvestmentCommand.amount ?: erC20Resource.getBalance(
            dQuickContract.blockchainGateway.network,
            quick,
            prepareInvestmentCommand.user
        )

    private fun getAllowance(prepareInvestmentCommand: PrepareInvestmentCommand) =
        erC20Resource.getAllowance(
            dQuickContract.blockchainGateway.network,
            quick,
            prepareInvestmentCommand.user,
            dQuickContract.address
        )

    suspend fun getInvestmentTransaction(prepareInvestmentCommand: PrepareInvestmentCommand): Deferred<PreparedTransaction?> =
        coroutineScope {
            async {
                val allowance = getAllowance(prepareInvestmentCommand)
                val requiredBalance = getWantBalance(prepareInvestmentCommand)

                if (allowance >= requiredBalance) {
                    prepareInvestmentCommand.amount?.let { amount ->
                        PreparedTransaction(
                            function = dQuickContract.enterFunction(amount)
                        )
                    } ?: PreparedTransaction(
                        function = dQuickContract.enterFunction(requiredBalance)
                    )
                } else {
                    null
                }
            }
        }
}