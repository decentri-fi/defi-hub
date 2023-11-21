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
import java.math.BigInteger

class BancorPoolInvestmentPreparer(
    erC20Resource: ERC20Resource,
    private val bancorNetworkContract: BancorNetworkContract,
    private val underlyingToken: String
) : InvestmentPreparer(erC20Resource) {

    override suspend fun getInvestmentTransaction(user: String, amount: BigInteger): PreparedTransaction {
        return PreparedTransaction(bancorNetworkContract.depositFunction(underlyingToken, amount))
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