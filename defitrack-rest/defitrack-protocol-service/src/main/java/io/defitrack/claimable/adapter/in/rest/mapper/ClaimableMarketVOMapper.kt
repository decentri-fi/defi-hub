package io.defitrack.claimable.adapter.`in`.rest.mapper

import io.defitrack.claim.ClaimableMarket
import io.defitrack.claimable.vo.ClaimableMarketVO
import io.defitrack.networkinfo.toNetworkInformation
import io.defitrack.protocol.mapper.ProtocolVOMapper
import org.springframework.stereotype.Component

@Component
class ClaimableMarketVOMapper(
    private val protocolVOMapper: ProtocolVOMapper
) {


    fun map(claimableMarket: ClaimableMarket): ClaimableMarketVO {
        return with(claimableMarket) {
            ClaimableMarketVO(
                id,
                name,
                network.toNetworkInformation(),
                protocolVOMapper.map(protocol),
                rewards = claimableRewardFetchers.flatMap { fetcher ->
                    fetcher.rewards.map { reward ->
                        ClaimableMarketVO.Reward(reward.token)
                    }
                }
            )
        }
    }
}