package io.defitrack.protocol.beefy.staking.invest

import io.defitrack.evm.contract.BlockchainGateway.Companion.MAX_UINT256
import io.defitrack.protocol.beefy.contract.BeefyVaultContract
import io.defitrack.staking.command.PrepareInvestmentCommand
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
                if (allowance < requiredBalance) {
                    PreparedTransaction(
                        function = beefyVault.approveFunction(
                            beefyVault.address,
                            MAX_UINT256.value
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
                    if (prepareInvestmentCommand.amount == null) {
                        PreparedTransaction(
                            function = beefyVault.depositAllFunction()
                        )
                    } else {
                        PreparedTransaction(
                            function = beefyVault.depositFunction(prepareInvestmentCommand.amount!!)
                        )
                    }
                } else {
                    null
                }
            }
        }
}