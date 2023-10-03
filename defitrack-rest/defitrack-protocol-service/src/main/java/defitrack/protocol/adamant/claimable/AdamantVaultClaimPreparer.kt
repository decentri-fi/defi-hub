package io.defitrack.protocol.adamant.claimable

import io.defitrack.claimable.ClaimingPreparer
import io.defitrack.claimable.PrepareClaimCommand
import io.defitrack.common.network.Network
import io.defitrack.network.toVO
import io.defitrack.protocol.adamant.AdamantVaultContract
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class AdamantVaultClaimPreparer(
    private val adamantVaultContract: AdamantVaultContract
) : ClaimingPreparer() {
    override suspend fun getClaimPreparation(prepareInvestmentCommand: PrepareClaimCommand?): Deferred<PreparedTransaction> =
        coroutineScope {
            async {
                PreparedTransaction(
                    function = adamantVaultContract.getClaimFunction(),
                    to = adamantVaultContract.address,
                    network = getNetwork().toVO()
                )
            }
        }

    override fun getEntryContract(): String {
        return adamantVaultContract.address
    }

    override fun getNetwork(): Network {
        return adamantVaultContract.blockchainGateway.network
    }
}