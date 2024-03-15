package io.defitrack.protocol.application.aave.v3.lending.invest

import io.defitrack.common.network.Network
import io.defitrack.erc20.port.`in`.ERC20Resource
import io.defitrack.invest.InvestmentPreparer
import io.defitrack.protocol.aave.v3.contract.PoolContract
import io.defitrack.transaction.PreparedTransaction
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