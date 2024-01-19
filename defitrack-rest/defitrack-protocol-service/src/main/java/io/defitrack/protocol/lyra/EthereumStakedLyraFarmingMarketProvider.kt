package io.defitrack.protocol.lyra

import arrow.core.nel
import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.LYRA)
class EthereumStakedLyraFarmingMarketProvider : FarmingMarketProvider() {

    val address = "0xcb9f85730f57732fc899fb158164b9ed60c77d49"

    override suspend fun fetchMarkets(): List<FarmingMarket> {

        val contract = StakedLyraContract(
            blockchainGateway = getBlockchainGateway(),
            address = address
        )

        val stakedToken = getToken(contract.stakedToken.await())
        val rewardToken = getToken(contract.rewardToken.await())

        return create(
            name = "Staked Lyra",
            identifier = address,
            stakedToken = stakedToken,
            rewardToken = rewardToken,
            positionFetcher = defaultPositionFetcher(contract.address),
            claimableRewardFetcher = ClaimableRewardFetcher(
                reward = Reward(
                    rewardToken,
                    contract::getTotalRewardsBalance
                ),
                preparedTransaction = { null }
            )
        ).nel()
    }

    override fun getProtocol(): Protocol {
        return Protocol.LYRA
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}