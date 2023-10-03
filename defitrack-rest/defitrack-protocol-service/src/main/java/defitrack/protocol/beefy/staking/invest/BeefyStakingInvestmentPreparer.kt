package io.defitrack.protocol.beefy.staking.invest

import io.defitrack.common.network.Network
import io.defitrack.invest.PrepareInvestmentCommand
import io.defitrack.market.farming.domain.InvestmentPreparer
import io.defitrack.network.toVO
import io.defitrack.protocol.beefy.contract.BeefyVaultContract
import io.defitrack.token.ERC20Resource
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope


class BeefyStakingInvestmentPreparer(
    private val beefyVault: BeefyVaultContract,
    erC20Resource: ERC20Resource
) : InvestmentPreparer(erC20Resource) {

    override suspend fun getToken(): String {
        return beefyVault.want()
    }

    override fun getEntryContract(): String {
        return beefyVault.address
    }

    override fun getNetwork(): Network {
        return beefyVault.blockchainGateway.network
    }

    override suspend fun getInvestmentTransaction(prepareInvestmentCommand: PrepareInvestmentCommand): Deferred<PreparedTransaction?> =
        coroutineScope {
            async {
                val requiredBalance = getInvestmentAmount(prepareInvestmentCommand)
                PreparedTransaction(
                    function = beefyVault.depositFunction(requiredBalance),
                    to = getEntryContract(),
                    network = beefyVault.blockchainGateway.network.toVO()
                )
            }
        }
}