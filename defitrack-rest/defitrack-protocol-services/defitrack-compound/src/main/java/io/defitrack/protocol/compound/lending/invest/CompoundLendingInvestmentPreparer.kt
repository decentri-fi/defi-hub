package io.defitrack.protocol.compound.lending.invest

import io.defitrack.invest.PrepareInvestmentCommand
import io.defitrack.protocol.compound.CompoundComptrollerContract
import io.defitrack.protocol.compound.CompoundTokenContract
import io.defitrack.staking.domain.InvestmentPreparer
import io.defitrack.token.ERC20Resource
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class CompoundLendingInvestmentPreparer(
    private val comptroller: CompoundComptrollerContract,
    private val ctoken: CompoundTokenContract,
    private val erC20Resource: ERC20Resource
) : InvestmentPreparer {


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
                            comptroller.blockchainGateway.network,
                            ctoken.underlyingAddress,
                            ctoken.address
                        )
                    )
                } else {
                    null
                }
            }
        }

    suspend fun getInvestmentTransaction(prepareInvestmentCommand: PrepareInvestmentCommand): Deferred<PreparedTransaction?> =
        coroutineScope {
            async {
                val allowance = getAllowance(prepareInvestmentCommand)
                val requiredBalance = getWantBalance(prepareInvestmentCommand)

                if (allowance >= requiredBalance) {
                    prepareInvestmentCommand.amount?.let { amount ->
                        PreparedTransaction(
                            function = ctoken.mintFunction(amount)
                        )
                    } ?: PreparedTransaction(
                        function = ctoken.mintFunction(requiredBalance)
                    )
                } else {
                    null
                }
            }
        }

    private fun getWantBalance(prepareInvestmentCommand: PrepareInvestmentCommand) =
        prepareInvestmentCommand.amount ?: erC20Resource.getBalance(
            comptroller.blockchainGateway.network,
            ctoken.underlyingAddress,
            prepareInvestmentCommand.user
        )

    private fun getAllowance(prepareInvestmentCommand: PrepareInvestmentCommand) =
        erC20Resource.getAllowance(
            comptroller.blockchainGateway.network,
            ctoken.underlyingAddress,
            prepareInvestmentCommand.user,
            ctoken.address
        )
}