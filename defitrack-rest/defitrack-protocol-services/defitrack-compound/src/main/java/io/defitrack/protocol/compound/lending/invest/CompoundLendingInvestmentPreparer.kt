package io.defitrack.protocol.compound.lending.invest

import io.defitrack.common.network.Network
import io.defitrack.invest.PrepareInvestmentCommand
import io.defitrack.market.farming.domain.InvestmentPreparer
import io.defitrack.network.toVO
import io.defitrack.protocol.compound.CompoundTokenContract
import io.defitrack.token.ERC20Resource
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class CompoundLendingInvestmentPreparer(
    private val ctoken: CompoundTokenContract,
    erC20Resource: ERC20Resource
) : InvestmentPreparer(erC20Resource) {

    override suspend fun getToken(): String {
        return ctoken.underlyingAddress()
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
                val requiredBalance = getInvestmentAmount(prepareInvestmentCommand)
                PreparedTransaction(
                    function = ctoken.mintFunction(requiredBalance),
                    to = getEntryContract(),
                    network = ctoken.blockchainGateway.network.toVO()
                )
            }
        }
}