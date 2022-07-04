package io.defitrack.protocol.balancer.claiming

import io.defitrack.claimable.ClaimingPreparer
import io.defitrack.claimable.PrepareClaimCommand
import io.defitrack.common.network.Network
import io.defitrack.network.toVO
import io.defitrack.protocol.balancer.contract.BalancerGaugeContract
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class BalancerClaimPreparer(
    private val gaugeContract: BalancerGaugeContract
) : ClaimingPreparer() {
    override suspend fun getClaimPreparation(prepareInvestmentCommand: PrepareClaimCommand): Deferred<PreparedTransaction?> =
        coroutineScope {
            async {
                PreparedTransaction(
                    function = gaugeContract.getClaimRewardsFunction(),
                    to = gaugeContract.address,
                    network = getNetwork().toVO()
                )
            }
        }

    override fun getEntryContract(): String {
        return gaugeContract.address
    }

    override fun getNetwork(): Network {
        return gaugeContract.blockchainGateway.network
    }
}