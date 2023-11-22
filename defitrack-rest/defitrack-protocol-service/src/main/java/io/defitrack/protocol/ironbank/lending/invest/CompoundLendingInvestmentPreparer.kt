package io.defitrack.protocol.ironbank.lending.invest

import io.defitrack.common.network.Network
import io.defitrack.invest.PrepareInvestmentCommand
import io.defitrack.market.farming.domain.InvestmentPreparer
import io.defitrack.network.toVO
import io.defitrack.protocol.ironbank.IronbankTokenContract
import io.defitrack.token.ERC20Resource
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.math.BigInteger

class CompoundLendingInvestmentPreparer(
    private val iToken: IronbankTokenContract,
    erC20Resource: ERC20Resource
) : InvestmentPreparer(erC20Resource) {

    override suspend fun getWant(): String {
        return iToken.underlyingAddress()
    }

    override fun getEntryContract(): String {
        return iToken.address
    }

    override fun getNetwork(): Network {
        return iToken.blockchainGateway.network
    }

    override suspend fun getInvestmentTransaction(user: String, amount: BigInteger): PreparedTransaction {
        return PreparedTransaction(iToken.mintFunction(amount))
    }
}