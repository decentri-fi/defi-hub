package io.defitrack.protocol.beefy.staking.invest

import io.defitrack.protocol.beefy.contract.BeefyVaultContract
import io.defitrack.invest.PrepareInvestmentCommand
import io.defitrack.staking.domain.InvestmentPreparer
import io.defitrack.token.ERC20Resource
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope


class BeefyStakingInvestmentPreparer(
    private val beefyVault: BeefyVaultContract,
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
                val want = beefyVault.want
                val allowance = erC20Resource.getAllowance(
                    beefyVault.blockchainGateway.network,
                    want,
                    prepareInvestmentCommand.user,
                    beefyVault.address
                )
                val requiredBalance =
                    prepareInvestmentCommand.amount ?: erC20Resource.getBalance(
                        beefyVault.blockchainGateway.network,
                        want,
                        prepareInvestmentCommand.user
                    )
                if (allowance < requiredBalance) {
                    PreparedTransaction(
                        function = erC20Resource.getFullApproveFunction(
                            beefyVault.blockchainGateway.network,
                            want,
                            beefyVault.address
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
                val allowance = erC20Resource.getAllowance(
                    beefyVault.blockchainGateway.network,
                    beefyVault.want,
                    prepareInvestmentCommand.user,
                    beefyVault.address
                )
                val requiredBalance =
                    prepareInvestmentCommand.amount ?: erC20Resource.getBalance(
                        beefyVault.blockchainGateway.network,
                        beefyVault.want,
                        prepareInvestmentCommand.user
                    )
                if (allowance >= requiredBalance) {
                    prepareInvestmentCommand.amount?.let { amount ->
                        PreparedTransaction(
                            function = beefyVault.depositFunction(amount)
                        )
                    } ?: PreparedTransaction(
                        function = beefyVault.depositAllFunction()
                    )
                } else {
                    null
                }
            }
        }
}