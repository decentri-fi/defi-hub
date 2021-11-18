package io.defitrack.staking

import io.codechef.defitrack.protocol.ProtocolService
import io.codechef.defitrack.staking.domain.RewardToken
import io.codechef.defitrack.staking.domain.StakedToken
import io.codechef.defitrack.staking.domain.StakingMarketElement
import io.defitrack.protocol.staking.Token

interface StakingMarketService : ProtocolService {
    fun getStakingMarkets(): List<StakingMarketElement>

    fun Token.toStakedToken(): StakedToken {
        return StakedToken(
            name = this.name,
            symbol = this.symbol,
            address = this.address,
            network = getNetwork(),
            decimals = this.decimals,
            type = this.type
        )
    }


    fun Token.toRewardToken(): RewardToken {
        return RewardToken(
            name = this.name,
            symbol = this.symbol,
            decimals = this.decimals,
        )
    }
}