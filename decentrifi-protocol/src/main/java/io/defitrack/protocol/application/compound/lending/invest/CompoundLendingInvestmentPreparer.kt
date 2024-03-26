package io.defitrack.protocol.application.compound.lending.invest

import io.defitrack.balance.BalanceResource
import io.defitrack.common.network.Network
import io.defitrack.erc20.port.`in`.ERC20Resource
import io.defitrack.invest.InvestmentPreparer
import io.defitrack.protocol.compound.v2.contract.CompoundTokenContract
import io.defitrack.transaction.PreparedTransaction
import java.math.BigInteger

class CompoundLendingInvestmentPreparer(
    private val ctoken: CompoundTokenContract,
    erC20Resource: ERC20Resource,
    balanceResource: BalanceResource
) : InvestmentPreparer(erC20Resource ,balanceResource) {

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