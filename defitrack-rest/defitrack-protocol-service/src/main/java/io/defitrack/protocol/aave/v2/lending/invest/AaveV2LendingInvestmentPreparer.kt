package io.defitrack.protocol.aave.v2.lending.invest

import io.defitrack.common.network.Network
import io.defitrack.invest.PrepareInvestmentCommand
import io.defitrack.market.farming.domain.InvestmentPreparer
import io.defitrack.network.toVO
import io.defitrack.protocol.aave.v2.contract.LendingPoolContract
import io.defitrack.token.ERC20Resource
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope


class AaveV2LendingInvestmentPreparer(
    private val token: String,
    private val lendingPoolContract: LendingPoolContract,
    erC20Resource: ERC20Resource
) : InvestmentPreparer(erC20Resource) {

    override suspend fun getInvestmentTransaction(prepareInvestmentCommand: PrepareInvestmentCommand): Deferred<PreparedTransaction?> =
        coroutineScope {
            async {
                val requiredBalance = getInvestmentAmount(prepareInvestmentCommand)
                PreparedTransaction(
                    function = lendingPoolContract.depositFunction(
                        token, requiredBalance
                    ),
                    to = getEntryContract(),
                    network = getNetwork().toVO()
                )
            }
        }

    override suspend fun getToken(): String {
        return token
    }

    override fun getEntryContract(): String {
        return lendingPoolContract.address
    }

    override fun getNetwork(): Network {
        return lendingPoolContract.blockchainGateway.network
    }
}