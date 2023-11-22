package io.defitrack.protocol.aave.v3.lending.invest

import io.defitrack.common.network.Network
import io.defitrack.invest.PrepareInvestmentCommand
import io.defitrack.market.farming.domain.InvestmentPreparer
import io.defitrack.network.toVO
import io.defitrack.protocol.aave.v3.contract.PoolContract
import io.defitrack.token.ERC20Resource
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.math.BigInteger


class AaveV3LendingInvestmentPreparer(
    private val underlying: String,
    private val poolContract: PoolContract,
    erC20Resource: ERC20Resource
) : InvestmentPreparer(erC20Resource) {

    override suspend fun getInvestmentTransaction(user: String, amount: BigInteger): PreparedTransaction {
        return PreparedTransaction(poolContract.getSupplyFunction(underlying, amount, user))
    }

    override suspend fun getWant(): String {
        return underlying
    }

    override fun getEntryContract(): String {
        return poolContract.address
    }

    override fun getNetwork(): Network {
        return poolContract.blockchainGateway.network
    }
}