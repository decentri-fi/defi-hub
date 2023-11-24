package io.defitrack.protocol.aura.staking

import arrow.core.nel
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aura.VoteLockedAuraContract
import org.springframework.stereotype.Component

@ConditionalOnCompany(Company.AURA)
@Component
class VoteLockedAuraProvider : FarmingMarketProvider() {

    val vlauraAddress = "0x3fa73f1e5d8a792c80f426fc8f84fbf7ce9bbcac"

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val token = getToken(vlauraAddress)
        val contract = VoteLockedAuraContract(getBlockchainGateway(), vlauraAddress)

        val rewards = contract.rewardTokens().map {
            getToken(it)
        }
        //todo: claimables

        return create(
            name = "Vote Locked Aura",
            identifier = vlauraAddress,
            stakedToken = token,
            rewardTokens = rewards,
            positionFetcher = PositionFetcher(contract::balances),
        ).nel()
    }

    override fun getProtocol(): Protocol {
        return Protocol.AURA
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}