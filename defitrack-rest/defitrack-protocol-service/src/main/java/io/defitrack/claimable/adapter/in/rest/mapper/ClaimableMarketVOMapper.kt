package io.defitrack.claimable.adapter.`in`.rest.mapper

import io.defitrack.claim.ClaimableMarket
import io.defitrack.claimable.adapter.`in`.rest.domain.ClaimableMarketVO
import io.defitrack.erc20.toVO
import io.defitrack.network.toVO
import io.defitrack.protocol.mapper.ProtocolVOMapper
import org.springframework.stereotype.Component

@Component
class ClaimableMarketVOMapper(
    private val protocolVOMapper: ProtocolVOMapper
) {


    fun map(claimableMarket: ClaimableMarket): ClaimableMarketVO {
        return with(claimableMarket) {
            ClaimableMarketVO(
                id = id,
                name = name,
                network = network.toVO(),
                protocolVOMapper.map(protocol),
                rewards = claimableRewardFetchers.flatMap { fetcher ->
                    fetcher.rewards.map { reward ->
                        ClaimableMarketVO.Reward(reward.token.toVO())
                    }
                }
            )
        }
    }
}