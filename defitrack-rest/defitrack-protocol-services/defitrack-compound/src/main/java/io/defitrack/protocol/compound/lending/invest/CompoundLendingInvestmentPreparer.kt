package io.defitrack.protocol.compound.lending.invest

import io.defitrack.common.network.Network
import io.defitrack.invest.PrepareInvestmentCommand
import io.defitrack.protocol.compound.CompoundTokenContract
import io.defitrack.staking.domain.InvestmentPreparer
import io.defitrack.token.ERC20Resource
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class CompoundLendingInvestmentPreparer(
    private val ctoken: CompoundTokenContract,
    erC20Resource: ERC20Resource
) : InvestmentPreparer(erC20Resource) {

    override fun getToken(): String {
        return ctoken.underlyingAddress
    }

    override fun getEntryContract(): String {
        return ctoken.address
    }

    override fun getNetwork(): Network {
        return ctoken.blockchainGateway.network
    }

    override suspend fun getInvestmentTransaction(prepareInvestmentCommand: PrepareInvestmentCommand): Deferred<PreparedTransaction?> =
        coroutineScope {
            async {
                val allowance = getAllowance(prepareInvestmentCommand)
                val requiredBalance = getInvestmentAmount(prepareInvestmentCommand)

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
}