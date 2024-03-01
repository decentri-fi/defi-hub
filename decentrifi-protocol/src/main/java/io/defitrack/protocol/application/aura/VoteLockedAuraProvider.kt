package io.defitrack.protocol.application.aura

import arrow.core.nel
import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aura.VoteLockedAuraContract
import org.springframework.stereotype.Component

@ConditionalOnCompany(Company.AURA)
@Component
class VoteLockedAuraProvider : FarmingMarketProvider() {

    val vlauraAddress = "0x3fa73f1e5d8a792c80f426fc8f84fbf7ce9bbcac"

    context(BlockchainGateway)
    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val token = getToken(vlauraAddress)
        val contract = VoteLockedAuraContract(vlauraAddress)

        val rewards = contract.rewardTokens().map {
            getToken(it)
        }
        //todo: claimables

        return create(
            name = "Vote Locked Aura",
            identifier = vlauraAddress,
            stakedToken = token,
            rewardTokens = rewards,
            type = "aura.vote-locked",
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