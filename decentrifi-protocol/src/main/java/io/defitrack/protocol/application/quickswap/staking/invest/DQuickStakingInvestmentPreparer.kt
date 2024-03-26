package io.defitrack.protocol.quickswap.staking.invest

import io.defitrack.balance.BalanceResource
import io.defitrack.common.network.Network
import io.defitrack.erc20.port.`in`.ERC20Resource
import io.defitrack.invest.InvestmentPreparer
import io.defitrack.protocol.quickswap.contract.DQuickContract
import io.defitrack.transaction.PreparedTransaction
import java.math.BigInteger

class DQuickStakingInvestmentPreparer(
    erC20Resource: ERC20Resource,
    balanceResource: BalanceResource,
    val dQuickContract: DQuickContract,
) : InvestmentPreparer(erC20Resource, balanceResource) {

    val quick = "0x831753dd7087cac61ab5644b308642cc1c33dc13"

    override suspend fun getWant(): String {
        return quick
    }

    override fun getEntryContract(): String {
        return dQuickContract.address
    }

    override fun getNetwork(): Network {
        return dQuickContract.getNetwork()
    }

    override suspend fun getInvestmentTransaction(user: String, amount: BigInteger): PreparedTransaction {
        return PreparedTransaction(dQuickContract.enterFunction(amount))
    }
}