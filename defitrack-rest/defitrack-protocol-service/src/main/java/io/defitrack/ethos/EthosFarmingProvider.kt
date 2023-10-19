package io.defitrack.ethos

import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.ethos.EthosStabilityPool
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.ETHOS)
class EthosFarmingProvider : FarmingMarketProvider() {

    val oath = "0x39fde572a18448f8139b7788099f0a0740f51205"
    val stabilityPoolAddress = "0x8b147a2d4fc3598079c64b8bf9ad2f776786cfed"

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val contract = EthosStabilityPool(
            getBlockchainGateway(), stabilityPoolAddress
        )

        val reward = getToken(oath).toFungibleToken()
        val staked = getToken(contract.ernToken.await()).toFungibleToken()

        return listOf(
            create(
                name = "Ethos Farming",
                identifier = stabilityPoolAddress,
                stakedToken = staked,
                rewardTokens = listOf(reward),
                positionFetcher = PositionFetcher(
                    contract.address,
                    contract::depositsFn,
                ),
                claimableRewardFetcher = ClaimableRewardFetcher(
                    Reward(
                        reward,
                        contract.address,
                        contract::claimableFn,
                    ),
                    preparedTransaction = selfExecutingTransaction(contract::claimFn)
                )
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.ETHOS
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}