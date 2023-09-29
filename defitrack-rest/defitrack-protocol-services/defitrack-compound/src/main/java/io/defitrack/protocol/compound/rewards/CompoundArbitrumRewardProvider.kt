package io.defitrack.protocol.compound.rewards

import io.defitrack.claimable.*
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.market.farming.ClaimableMarketProvider
import io.defitrack.network.toVO
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.compound.CompoundAddressesProvider
import io.defitrack.protocol.compound.v3.contract.CompoundRewardContract
import io.defitrack.protocol.compound.v3.contract.CompoundV3AssetContract
import io.defitrack.transaction.PreparedTransaction
import org.springframework.stereotype.Component

@Component
class CompoundArbitrumRewardProvider(
) : CompoundRewardProvider() {

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}