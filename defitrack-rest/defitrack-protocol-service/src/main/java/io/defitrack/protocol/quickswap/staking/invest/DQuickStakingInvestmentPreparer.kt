package io.defitrack.protocol.quickswap.staking.invest

import io.defitrack.common.network.Network
import io.defitrack.invest.PrepareInvestmentCommand
import io.defitrack.market.farming.domain.InvestmentPreparer
import io.defitrack.network.toVO
import io.defitrack.protocol.quickswap.contract.DQuickContract
import io.defitrack.token.ERC20Resource
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.math.BigInteger

class DQuickStakingInvestmentPreparer(
    erC20Resource: ERC20Resource,
    val dQuickContract: DQuickContract,
) : InvestmentPreparer(erC20Resource) {

    val quick = "0x831753dd7087cac61ab5644b308642cc1c33dc13"

    override suspend fun getToken(): String {
        return quick
    }

    override fun getEntryContract(): String {
        return dQuickContract.address
    }

    override fun getNetwork(): Network {
        return dQuickContract.blockchainGateway.network
    }

    override suspend fun getInvestmentTransaction(user: String, amount: BigInteger): PreparedTransaction {
        return PreparedTransaction(dQuickContract.enterFunction(amount))
    }
}