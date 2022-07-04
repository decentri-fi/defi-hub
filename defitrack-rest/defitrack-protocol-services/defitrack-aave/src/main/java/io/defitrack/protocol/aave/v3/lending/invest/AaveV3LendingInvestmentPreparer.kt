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


class AaveV3LendingInvestmentPreparer(
    private val underlying: String,
    private val poolContract: PoolContract,
    erC20Resource: ERC20Resource
) : InvestmentPreparer(erC20Resource) {

    override suspend fun getInvestmentTransaction(prepareInvestmentCommand: PrepareInvestmentCommand): Deferred<PreparedTransaction?> =
        coroutineScope {
            async {
                val allowance = getAllowance(prepareInvestmentCommand)
                val requiredBalance = getInvestmentAmount(prepareInvestmentCommand)

                if (allowance >= requiredBalance) {
                    prepareInvestmentCommand.amount?.let { amount ->
                        PreparedTransaction(
                            function = poolContract.getSupplyFunction(
                                underlying, requiredBalance, prepareInvestmentCommand.user
                            ),
                            to = getEntryContract(),
                            network = getNetwork().toVO()
                        )
                    } ?: PreparedTransaction(
                        function = poolContract.getSupplyFunction(
                            underlying, requiredBalance, prepareInvestmentCommand.user
                        ),
                        to = getEntryContract(),
                        network = getNetwork().toVO()
                    )
                } else {
                    null
                }
            }
        }

    override suspend fun getToken(): String {
        return underlying
    }

    override fun getEntryContract(): String {
        return poolContract.address
    }

    override fun getNetwork(): Network {
        return poolContract.blockchainGateway.network
    }
}