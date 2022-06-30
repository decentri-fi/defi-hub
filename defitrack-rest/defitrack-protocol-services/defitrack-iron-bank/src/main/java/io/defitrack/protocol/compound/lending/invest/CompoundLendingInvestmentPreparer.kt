package io.defitrack.protocol.compound.lending.invest

import io.defitrack.common.network.Network
import io.defitrack.invest.PrepareInvestmentCommand
import io.defitrack.market.farming.domain.InvestmentPreparer
import io.defitrack.protocol.compound.IronbankTokenContract
import io.defitrack.token.ERC20Resource
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class CompoundLendingInvestmentPreparer(
    private val iToken: IronbankTokenContract,
    erC20Resource: ERC20Resource
) : InvestmentPreparer(erC20Resource) {

    override suspend fun getToken(): String {
        return iToken.underlyingAddress()
    }

    override fun getEntryContract(): String {
        return iToken.address
    }

    override fun getNetwork(): Network {
        return iToken.blockchainGateway.network
    }

    override suspend fun getInvestmentTransaction(prepareInvestmentCommand: PrepareInvestmentCommand): Deferred<PreparedTransaction?> =
        coroutineScope {
            async {
                val allowance = getAllowance(prepareInvestmentCommand)
                val requiredBalance = getInvestmentAmount(prepareInvestmentCommand)

                if (allowance >= requiredBalance) {
                    prepareInvestmentCommand.amount?.let { amount ->
                        PreparedTransaction(
                            function = iToken.mintFunction(amount),
                            to = getEntryContract()
                        )
                    } ?: PreparedTransaction(
                        function = iToken.mintFunction(requiredBalance),
                        to = getEntryContract()
                    )
                } else {
                    null
                }
            }
        }
}