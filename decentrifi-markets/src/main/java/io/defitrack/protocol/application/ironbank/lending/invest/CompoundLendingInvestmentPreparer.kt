package io.defitrack.protocol.application.ironbank.lending.invest

import io.defitrack.common.network.Network
import io.defitrack.domain.BalanceResource
import io.defitrack.invest.InvestmentPreparer
import io.defitrack.port.output.ERC20Client
import io.defitrack.protocol.ironbank.IronbankTokenContract
import io.defitrack.transaction.PreparedTransaction
import java.math.BigInteger

class CompoundLendingInvestmentPreparer(
    private val iToken: IronbankTokenContract,
    erC20Resource: ERC20Client,
    balanceResource: BalanceResource
) : InvestmentPreparer(erC20Resource, balanceResource) {

    override suspend fun getWant(): String {
        return iToken.underlyingAddress()
    }

    override fun getEntryContract(): String {
        return iToken.address
    }

    override fun getNetwork(): Network {
        return iToken.getNetwork()
    }

    override suspend fun getInvestmentTransaction(user: String, amount: BigInteger): PreparedTransaction {
        return PreparedTransaction(iToken.mintFunction(amount))
    }
}