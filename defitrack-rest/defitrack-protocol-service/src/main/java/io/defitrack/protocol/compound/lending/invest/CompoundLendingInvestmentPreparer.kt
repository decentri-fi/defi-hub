package io.defitrack.protocol.compound.lending.invest

import io.defitrack.common.network.Network
import io.defitrack.invest.PrepareInvestmentCommand
import io.defitrack.market.farming.domain.InvestmentPreparer
import io.defitrack.network.toVO
import io.defitrack.protocol.compound.v2.contract.CompoundTokenContract
import io.defitrack.token.ERC20Resource
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.math.BigInteger

class CompoundLendingInvestmentPreparer(
    private val ctoken: CompoundTokenContract,
    erC20Resource: ERC20Resource
) : InvestmentPreparer(erC20Resource) {

    override suspend fun getWant(): String {
        return ctoken.getUnderlyingAddress()
    }

    override fun getEntryContract(): String {
        return ctoken.address
    }

    override fun getNetwork(): Network {
        return ctoken.blockchainGateway.network
    }

    override suspend fun getInvestmentTransaction(user: String, amount: BigInteger): PreparedTransaction {
        return PreparedTransaction(ctoken.mintFunction(amount))
    }
}