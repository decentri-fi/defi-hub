package io.defitrack.protocol.application.bancor

import io.defitrack.common.network.Network
import io.defitrack.domain.BalanceResource
import io.defitrack.invest.InvestmentPreparer
import io.defitrack.port.output.ERC20Client
import io.defitrack.protocol.bancor.contract.BancorNetworkContract
import io.defitrack.transaction.PreparedTransaction
import java.math.BigInteger

class BancorPoolInvestmentPreparer(
    erC20Resource: ERC20Client,
    balanceResource: BalanceResource,
    private val bancorNetworkContract: BancorNetworkContract,
    private val underlyingToken: String
) : InvestmentPreparer(erC20Resource, balanceResource) {

    override suspend fun getInvestmentTransaction(user: String, amount: BigInteger): PreparedTransaction {
        return PreparedTransaction(bancorNetworkContract.depositFunction(underlyingToken, amount))
    }

    override suspend fun getWant(): String {
        return underlyingToken
    }

    override fun getEntryContract(): String {
        return bancorNetworkContract.address
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}