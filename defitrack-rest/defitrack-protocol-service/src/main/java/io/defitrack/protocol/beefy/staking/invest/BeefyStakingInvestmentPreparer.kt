package io.defitrack.protocol.beefy.staking.invest

import io.defitrack.common.network.Network
import io.defitrack.erc20.port.`in`.ERC20Resource
import io.defitrack.invest.InvestmentPreparer
import io.defitrack.protocol.beefy.contract.BeefyVaultContract
import io.defitrack.transaction.PreparedTransaction
import java.math.BigInteger


class BeefyStakingInvestmentPreparer(
    private val beefyVault: BeefyVaultContract,
    erC20Resource: ERC20Resource
) : InvestmentPreparer(erC20Resource) {

    override suspend fun getWant(): String {
        return beefyVault.want()
    }

    override fun getEntryContract(): String {
        return beefyVault.address
    }

    override fun getNetwork(): Network {
        return beefyVault.blockchainGateway.network
    }

    override suspend fun getInvestmentTransaction(user: String, amount: BigInteger): PreparedTransaction {
        return PreparedTransaction(beefyVault.depositFunction(amount))
    }
}