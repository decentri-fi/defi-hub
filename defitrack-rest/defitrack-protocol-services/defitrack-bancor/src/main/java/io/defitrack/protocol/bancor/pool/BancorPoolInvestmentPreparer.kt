package io.defitrack.protocol.bancor.pool

import io.defitrack.common.network.Network
import io.defitrack.invest.PrepareInvestmentCommand
import io.defitrack.market.farming.domain.InvestmentPreparer
import io.defitrack.network.toVO
import io.defitrack.protocol.bancor.contract.BancorNetworkContract
import io.defitrack.token.ERC20Resource
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class BancorPoolInvestmentPreparer(
    erC20Resource: ERC20Resource,
    private val bancorNetworkContract: BancorNetworkContract,
    private val underlyingToken: String
) : InvestmentPreparer(erC20Resource) {

    override suspend fun getInvestmentTransaction(prepareInvestmentCommand: PrepareInvestmentCommand): Deferred<PreparedTransaction?> {
        return coroutineScope {
            async {
                val requiredBalance = getInvestmentAmount(prepareInvestmentCommand)

                prepareInvestmentCommand.amount?.let { amount ->
                    PreparedTransaction(
                        function = bancorNetworkContract.depositFunction(underlyingToken, amount),
                        to = getEntryContract(),
                        network = bancorNetworkContract.blockchainGateway.network.toVO()
                    )
                } ?: PreparedTransaction(
                    function = bancorNetworkContract.depositFunction(underlyingToken, requiredBalance),
                    to = getEntryContract(),
                    network = bancorNetworkContract.blockchainGateway.network.toVO()
                )
            }
        }
    }

    override suspend fun getToken(): String {
        return underlyingToken
    }

    override fun getEntryContract(): String {
        return bancorNetworkContract.address
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}