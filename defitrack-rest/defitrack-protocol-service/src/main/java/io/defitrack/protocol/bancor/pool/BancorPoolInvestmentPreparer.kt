package io.defitrack.protocol.bancor.pool

import io.defitrack.common.network.Network
import io.defitrack.erc20.port.`in`.ERC20Resource
import io.defitrack.invest.InvestmentPreparer
import io.defitrack.protocol.bancor.contract.BancorNetworkContract
import io.defitrack.transaction.PreparedTransaction
import java.math.BigInteger

class BancorPoolInvestmentPreparer(
    erC20Resource: ERC20Resource,
    private val bancorNetworkContract: BancorNetworkContract,
    private val underlyingToken: String
) : InvestmentPreparer(erC20Resource) {

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