package io.defitrack.protocol.aave.v2.lending.invest

import io.defitrack.common.network.Network
import io.defitrack.erc20.port.`in`.ERC20Resource
import io.defitrack.invest.InvestmentPreparer
import io.defitrack.protocol.aave.v2.contract.LendingPoolContract
import io.defitrack.transaction.PreparedTransaction
import java.math.BigInteger


class AaveV2LendingInvestmentPreparer(
    private val token: String,
    private val lendingPoolContract: LendingPoolContract,
    erC20Resource: ERC20Resource
) : InvestmentPreparer(erC20Resource) {

    override suspend fun getInvestmentTransaction(user: String, amount: BigInteger): PreparedTransaction {
        return PreparedTransaction(lendingPoolContract.depositFunction(token, amount))
    }

    override suspend fun getWant(): String {
        return token
    }

    override fun getEntryContract(): String {
        return lendingPoolContract.address
    }

    override fun getNetwork(): Network {
        return lendingPoolContract.blockchainGateway.network
    }
}