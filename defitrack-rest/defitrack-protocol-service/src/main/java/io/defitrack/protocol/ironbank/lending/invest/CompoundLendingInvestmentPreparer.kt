package io.defitrack.protocol.ironbank.lending.invest

import io.defitrack.common.network.Network
import io.defitrack.erc20.port.`in`.ERC20Resource
import io.defitrack.invest.InvestmentPreparer
import io.defitrack.protocol.ironbank.IronbankTokenContract
import io.defitrack.transaction.PreparedTransaction
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
        return iToken.getNetwork()
    }

    override suspend fun getInvestmentTransaction(user: String, amount: BigInteger): PreparedTransaction {
        return PreparedTransaction(iToken.mintFunction(amount))
    }
}