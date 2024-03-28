package io.defitrack.protocol.application.compound.lending.invest

import io.defitrack.common.network.Network
import io.defitrack.domain.BalanceResource
import io.defitrack.invest.InvestmentPreparer
import io.defitrack.port.output.ERC20Client
import io.defitrack.protocol.compound.v2.contract.CompoundTokenContract
import io.defitrack.transaction.PreparedTransaction
import java.math.BigInteger

class CompoundLendingInvestmentPreparer(
    private val ctoken: CompoundTokenContract,
    erC20Resource: ERC20Client,
    balanceResource: BalanceResource
) : InvestmentPreparer(erC20Resource, balanceResource) {

    override suspend fun getWant(): String {
        return ctoken.getUnderlyingAddress()
    }

    override fun getEntryContract(): String {
        return ctoken.address
    }

    override fun getNetwork(): Network {
        return ctoken.getNetwork()
    }

    override suspend fun getInvestmentTransaction(user: String, amount: BigInteger): PreparedTransaction {
        return PreparedTransaction(ctoken.mintFunction(amount))
    }
}