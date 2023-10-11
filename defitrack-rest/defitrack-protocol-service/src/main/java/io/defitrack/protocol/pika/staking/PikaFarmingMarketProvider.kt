package io.defitrack.protocol.pika.staking

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.pika.PikaStakingContract
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.PIKA)
class PikaFarmingMarketProvider : FarmingMarketProvider() {

    val pikaStaking = "0x323c8b8306d8d10d7fb78151b6d4be6f160f240a"

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val contract = PikaStakingContract(
            getBlockchainGateway(), pikaStaking
        )

        val rewardPools = contract.rewardPools()
        val stakingToken = getToken(contract.stakingToken.await())
        val rewardTokens = rewardPools.map { getToken(it.getRewardToken()) }

        return listOf(
            create(
                name = "Pika Staking",
                identifier = pikaStaking,
                stakedToken = stakingToken.toFungibleToken(),
                rewardTokens = rewardTokens.map(TokenInformationVO::toFungibleToken),
                balanceFetcher = defaultPositionFetcher(pikaStaking),
                internalMetadata = mapOf(
                    "contract" to contract
                )
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.PIKA
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}